LooperBoy : PedalBoy {
	var
	<>rec_trigger,
	<>play_trigger,
	<>looper_dict,
	<>loop_group,
	<>looper_buttons,
	<>buffers,
	<>rec_synth,
	<>play_synths,
	<>phasor_buses;

	*looper{|server, in, out, group|
		^super.new(
			server: server,
			in: in,
			out: out,
			group: group).init(server, in, out, group).looperinit;
	}

	init{|server, in, out, group|
		server.postln;
		num_active = num_active + 1;
		this.instance_id = num_active;
		this.server = server;
		this.in = in;
		this.out = out;
		this.group = group;
		this.gui_objs = List.new();
		this.scope_bus = Bus.audio(this.server, 1);

		if(server.isNil.not, {
			NodeWatcher.newFrom(server);
		});

		if(this.arg_dict.isNil, {
			this.arg_dict = Dictionary.new();
			this.mididef_dict = Dictionary.new();
		});
		this.arg_dict[\in] = this.in;
		this.arg_dict[\out] = this.out;
		this.loop_group = Group.new(this.group, \addAfter);
		this.looperinit;
	}

	looperinit{
		if(this.server.isNil.not,{
			this.synthdef = \looper;
			this.addaction = \addAfter;
			this.mappable_args = Dictionary.with(*[
				\dry -> MappableArg.dry(Bus.control(this.server, 1)).default_value_(0.8),
				\wet -> MappableArg.wet(Bus.control(this.server, 1)).default_value_(1),
				\gain -> MappableArg.gain(Bus.control(this.server, 1)),
				\rate -> MappableArg.new(
					symbol: \rate,
					bounds: 0.125 @ 8,
					default_value: 1,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(this.server, 1)),
			]);
			this.set_mappable_args(this.mappable_args);

			this.buffers = List.new();
			this.phasor_buses = List.new();
			this.play_synths = List.new();

			this.phasor_buses.add(Bus.control(this.server, 1));
			this.buffers.add(Buffer.alloc(
				server: this.server,
				numFrames: this.server.sampleRate*60*5,
				numChannels: 1));

			this.group.postln;
			SynthDef(\looper_recorder, {
				arg in, out, buffer, rate, gain, trig, phasor_bus;
				var sig, phasor;



				in = In.ar(in, 1);

				// in.poll;

				phasor = Phasor.ar(
					rate: BufRateScale.kr(buffer),
					start: 0,
					end: BufFrames.kr(buffer),
					resetPos: 0);

				// phasor.poll;

				Out.kr(phasor_bus, RunningMax.kr(phasor));


				RunningMax.kr(phasor).poll;
/*				phasor_bus.poll;
				phasor.poll;*/
				BufWr.ar(
					inputArray: in,
					bufnum: buffer,
					phase: phasor,
					loop: 1.0);


				FreeSelf.kr(trig);
			}).add;

			SynthDef(\looper_player, {
				arg buffer, out, rate =1, gain, phasor_bus;
				var sig, phasor;


				phasor_bus = In.kr(phasor_bus);
				phasor_bus.poll;

				phasor = Phasor.ar(/*
					trig: Impulse.kr((BufSampleRate.kr(buffer)/phasor_bus)).range(-1, 1),*/
					rate: BufRateScale.kr(buffer) * rate,
					start: 0,
					end: RunningMax.ar(K2A.ar(phasor_bus)),
					resetPos: 0);
/*
				BufFrames.kr(buffer).poll;
				(BufRateScale.kr(buffer) * rate).poll;
				Impulse.ar(BufSampleRate.kr(buffer)/phasor_bus).range(-1, 1).poll;
				phasor_bus.poll;*/
				// phasor_bus.poll;
				// phasor.poll;
				// Impulse.ar(1/phasor_bus).range(-1, 1).poll;

/*				phasor = Phasor.ar(
					rate: BufRateScale.kr(buffer) * rate,
					start: 0,
					end: phasor_bus,
					resetPos: 0);*/

				// phasor = Line.ar(0, phasor_bus, phasor_bus/BufSampleRate.kr(buffer)/rate);

			/*	phasor_bus.poll;
				phasor.poll;*/
				sig = BufRd.ar(
					numChannels: 1,
					bufnum: buffer,
					phase: phasor,
					loop: 0,
					interpolation: 1);

				// sig.poll;

				Out.ar(out, sig);
			}).add;
		});
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

		this.mididef_dict.add(
			\rec -> MIDIdef.noteOn(
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
		);
		this.mididef_dict.add(
			\clear -> MIDIdef.noteOn(
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
		);
	}

	node{
		^this.loop_group;
	}

	on{
		//only create a new node on the server if there isn't already one
		if(this.loop_group.isPlaying, {
			this.loop_group.run(true);
			this.scope_node.run(true); // creates the scope synthdef
		}, {
			this.loop_group = Group.new(this.group, \addAfter);
			this.loop_group.register;
			Routine({
				0.2.wait;
				this.scope()
			}).play;
		});
	}

	bypass{
		this.loop_group.run(false);
		this.scope_node.run(false);
	}

	free{
		this.loop_group.deepFree;
		this.scope_node.free;
	}


	is_bypassed{
		^this.node.isRunning.not;
	}

	scope{
		var out;
		// this.synth_node.postln
		// this.out.postln;

		if (this.out.isKindOf(Bus).not, {
			out = this.out;
		}, {
			out = this.out.index;
		});

		this.scope_node = SynthDef((this.synthdef.asString ++ "_scope").asSymbol,
			{arg in, out;
				var sig;
				sig = In.ar(in, 1);
				Out.ar(out, sig);
		}).play(this.loop_group, [\in, out, \out, this.scope_bus.index], \addToTail);
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
					["REC", Color.new255(51, 51, 51),  Color.fromHexString("FF1D15").vary],
					["STOP_REC", Color.new255(51, 51, 51), Color.fromHexString("FFFC31").vary],
				])
				.action_({
					arg button;
					button.value.switch(
						1, {
							var buffer, phasor_bus;
							buffer = this.buffers.at(this.buffers.size-1);
							phasor_bus = this.phasor_buses.at(this.phasor_buses.size-1);
/*							buffer.postln;
							phasor_bus.postln;*/

							"recording".postln;

							// this.arg_dict[\in].postln;

							this.arg_dict.add(\phasor_bus -> phasor_bus);
							this.arg_dict.add(\buffer -> buffer);

							// this.arg_dict.postln;

							this.rec_synth = Synth(\looper_recorder, this.arg_dict.asPairs, this.scope_node, \addBefore);
							// this.play_synths.add(Synth(\looper_player, this.arg_dict, this.loop_group, \addToTail));
						},
						0, {
							var buffer, phasor_bus;
							buffer = this.buffers.at(this.buffers.size-1);
							phasor_bus = this.phasor_buses.at(this.phasor_buses.size-1);
/*							buffer.postln;
							phasor_bus.postln;*/

							"playing".postln;

							this.arg_dict.add(\phasor_bus -> phasor_bus);
							this.arg_dict.add(\buffer -> buffer);

							this.rec_synth.free();
							// this.rec_synth = Synth(\looper_recorder, this.arg_dict, this.loop_group, \addToTail);
							this.play_synths.add(Synth(\looper_player, this.arg_dict.asPairs, this.scope_node, \addBefore));
							this.buffers.add(Buffer.alloc(
								server: this.server,
								numFrames: this.server.sampleRate*60*5,
								numChannels: 1));
							this.phasor_buses.add(Bus.control(this.server, 1));
						}
					);
				}),
				Button.new()
				.states_([
					["UNDO", Color.new255(51, 51, 51), Color.magenta],
				])
				.action_({
					arg button;
					button.value.switch(
						0, {
							this.play_synths.at(this.play_synths.size-1).free;
							this.buffers.at(this.buffers.size-1).free;
							this.phasor_buses.at(this.phasor_buses.size-1).free;
						}
					);
				}),
				Button.new()
				.states_([
					["CLEAR", Color.new255(51, 51, 51), Color.cyan],
				])
				.action_({
					arg button;
					button.value.switch(
						0, {
							this.buffers.do({
								arg b;
								b.free
							});
							this.phasor_buses.do({
								arg b;
								b.free
							});
							this.free;
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
