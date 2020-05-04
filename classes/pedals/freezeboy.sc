FreezeBoy : PedalBoy {
	var
	<>rec_trigger,
	<>play_trigger,
	<>looper_dict,
	<>looper_buttons;

	*freeze_boy{|server, in, out, group|
		^super.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(0.8),
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(1),
				\rate -> MappableArg.new(
					symbol: \rate,
					bounds: 0.125 @ 8,
					default_value: 1,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
			]),
			ugen_func: {
				arg in, out, buffer, rec_run, rec_trigger, rate, play_run = 1, play_trigger, clear_trigger,
				wet, dry, stretch = 1, overlap = 4;
				var sig, rec_phasor, play_phasor, trigRate, centerPos, trigger, dur, local;

				in = In.ar(in, 1);
				rec_phasor = Phasor.ar(
					trig: rec_trigger,
					rate: BufRateScale.kr(buffer),
					start: 0,
					end: BufFrames.kr(buffer),
					resetPos: 0) * rec_run;

				play_phasor = Phasor.ar(
					trig: play_trigger,
					rate: BufRateScale.kr(buffer) * rate,
					start: 0,
					end: RunningMax.ar(
						in: rec_phasor,
						trig: clear_trigger));


				sig = BufRd.ar(
					numChannels: 1,
					bufnum: buffer,
					phase: play_phasor,
					loop: 1,
					interpolation: 2) * play_run;


				dur = BufDur.kr(buffer)/ 100;
				trigRate = overlap / dur;
				centerPos = BufDur.kr(buffer) * 0.25;
				trigger = Impulse.ar(trigRate);

				centerPos = Phasor.ar(
					// trig: Impulse.ar(BufDur.kr(buffer).reciprocal),
					rate:   BufRateScale.kr(buffer)/ stretch,
					start: 0,
					end: BufFrames.kr(buffer)
				);

				// centerPos.poll;

/*				sig = GrainBuf.ar(
					numChannels: 1,
					trigger: trigger,
					dur: dur,
					sndbuf: buffer,
					rate: BufRateScale.kr(buffer) * rate,
					pos: 0,
					interp: 2,
					pan: 0,
					envbufnum: -1);*/


				 sig = TGrains.ar(
				 	numChannels: 2,
				 	trigger: trigger,
				 	bufnum: buffer,
				 	rate: rate,
					centerPos: centerPos,
				 	dur: dur,
				 	pan: 0,
				 	amp: 0.3,
					interp: 4);

/*				sig = PlayBuf.ar(
					numChannels: 1,
					bufnum: buffer,
					rate: BufRateScale.kr(buffer) * rate,
					trigger: play_trigger,
					startPos: 0.0,
					loop: 1,
					doneAction: 0) * play_run;*/

/*				play_trigger.poll;
				play_run.poll;*/
				// sig.poll;


				RecordBuf.ar(
					inputArray: in,
					bufnum: buffer,
					offset: rec_phasor,
					recLevel: 1.0,
					preLevel: 0,
					run: rec_run,
					loop: 0.0,
					trigger: rec_trigger,
					doneAction: 0);

				// rec_trigger.poll;
				// (rec_phasor / BufSampleRate.kr(buffer)).poll;
				// Delay1.ar(RunningMax.ar(rec_phasor, Delay1.ar(clear_trigger))/BufSampleRate.kr(buffer)).poll;

				sig = Mix.ar([sig * wet, dry * in]);



				ReplaceOut.ar(out, sig);
			},
			name: \freeze_boy,
			addaction: \addAfter
		).looperinit;
	}

	looperinit{
		var rec_run, rec_trigger, play_run, play_trigger, clear_trigger;

		var loop_buffer = Buffer.alloc(
			server: this.server,
			numFrames: this.server.sampleRate*0.2,
			numChannels: 1);

		this.looper_dict = Dictionary.new();

		rec_run = Bus.control(this.server, 1).set(0);
		rec_trigger = Bus.control(this.server, 1).set(-1);

		play_run = Bus.control(this.server, 1).set(0);
		play_trigger = Bus.control(this.server, 1).set(-1);

		clear_trigger = Bus.control(this.server, 1).set(-1);

		this.arg_dict.add(\buffer -> loop_buffer.bufnum);
		this.arg_dict.add(\rec_run -> rec_run.asMap);
		this.arg_dict.add(\rec_trigger -> rec_trigger.asMap);
		this.arg_dict.add(\play_run -> play_run.asMap);
		this.arg_dict.add(\play_trigger -> play_trigger.asMap);
		this.arg_dict.add(\clear_trigger -> clear_trigger.asMap);

		this.looper_dict.add(\buffer -> loop_buffer);
		this.looper_dict.add(\rec_run -> rec_run);
		this.looper_dict.add(\rec_trigger -> rec_trigger);
		this.looper_dict.add(\play_run -> play_run);
		this.looper_dict.add(\play_trigger -> play_trigger);
		this.looper_dict.add(\clear_trigger -> clear_trigger);
	}



	make_view{|parent, bounds|
		this.master_bounds = 200@300;
		//towards scalable pedals. this was the original size used during prototyping,
		/// and the views will continue to be scaled accordingly.

		if(bounds.isKindOf(Point), {
			"ERROR: you cant put a point in these bounds"
		});
		this.view = View(parent, bounds)
		.background_(Color.rand(0.75, 0.95));

		this.view_label;
		this.make_scope_view;
		this.add_gui_controls;
		this.add_buttons;

		this.view.layout_(
			VLayout(
				this.label_view,
				this.scope_view,
				this.control_view,
				this.looper_buttons,
				this.bypass_button
			)
		);
	}

	assign_loop_controls{
		|rec_butt_midinote, clear_butt_midinote|
		MIDIdef.noteOn(
			key: ("rec_" ++ this.synthdef.asString).asSymbol,
			func: {
				arg vel, note;
				note.postln;
				Routine({
					if (note == rec_butt_midinote,{
						var val;
						if(this.looper_buttons.children[0].value == 0, {val = 1}, {val = 0});
						this.looper_buttons.children[0].valueAction_(val);

					});
				}).play(AppClock)
		});
		MIDIdef.noteOn(
			key: ("clear_" ++ this.synthdef.asString).asSymbol,
			func: {
				arg vel, note;
				note.postln;
				Routine({
					if (note == clear_butt_midinote,{
						var val;
						this.looper_buttons.children[1].valueAction_(0);
						"cleared"
					});
				}).play(AppClock)
		});
	}



	add_buttons{
		var b = this.master_bounds;
		var a = this.view.bounds.extent;
		var bypass_button;

		this.looper_buttons = View(
			parent: this.view,
			bounds: Rect(0, 0, this.view.bounds.width-10, 20@a.y/b.y));

		this.looper_buttons.layout_(
			HLayout(
				Button.new()
				.states_([
					["SUS", Color.new255(51, 51, 51),  Color.fromHexString("FF1D15").vary],
				])
				.action_({
					arg button;
					button.value.switch(
						0, {

							this.looper_dict[\rec_run].setSynchronous(1);
							this.looper_dict[\rec_trigger].setSynchronous(1);
							this.looper_dict[\clear_trigger].set(-1);
							this.looper_dict[\play_run].setSynchronous(1);
							this.looper_dict[\play_trigger].setSynchronous(1);
						},
					);
				}),

				Button.new()
				.states_([
					["REL", Color.new255(51, 51, 51), Color.new(0.49.rand(0.5), 0.89.rand(0.9), 0.89.rand(0.9))],
				])
				.action_({
					arg button;
					button.value.switch(
						0, {
							this.looper_dict[\rec_trigger].set(-1);
							this.looper_dict[\rec_run].setSynchronous(0);

							this.looper_dict[\play_run].setSynchronous(0);
							this.looper_dict[\play_trigger].setSynchronous(-1);
							this.looper_dict[\clear_trigger].set(1);
						}
					);
				})
			)
		);


		this.bypass_button = Button.new(
			parent: this.view,
			bounds: Rect(0, 0, this.view.bounds.width-15, 20*a.y/b.y))
		.minSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height))
		.maxSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height))
		.states_([
			["BYPASS", Color.new255(51, 51, 51), Color.new(0.9, 0.5, 0.5)],
			["ON", Color.new255(51, 51, 51), Color.new(0.5, 0.9, 0.5)]])
		.action_({
			arg button;
			button.value.switch(
				0, {this.on; button.background_(Color.new(0.9, 0.5, 0.5))},
				1, {this.bypass; button.background_(Color.new(0.5, 0.9, 0.5))});

		});

	}

}
