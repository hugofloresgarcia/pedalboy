
FreezeBoy : PedalBoy {
	var
	<>sustain_midinote,
	<>sustain_buf;


	*new{|server, in, out, group, sustain_midinote|
		var sustain_buf = Buffer.alloc(server, 48000 * 1);
		^super.from_synth_params(

			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\wet -> MappableArg.wet(Bus.control(server, 1)),
				\dry -> MappableArg.dry(Bus.control(server, 1)),
				\window -> MappableArg.new(
					symbol: \window,
					bounds: 0.01@1,
					default_value: 0.1,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, bufnum, wet, dry, window, rate, trigger;
				var sig;

				in = In.ar(in);

				sig = RecordBuf.ar(
					inputArray: in,
					bufnum: bufnum,
					offset: 0.0,
					recLevel: 1,
					preLevel: 0,
					run: trigger-0.5,
					loop: 0,
					trigger: trigger-0.5,
					doneAction: 0);


				sig = PlayBuf.ar(
					numChannels: 1,
					bufnum: bufnum,
					rate: BufRateScale.kr(bufnum) * rate,
					trigger: trigger,
					startPos: 0.0,
					loop: 1,
					doneAction: 0);


				sig = Mix.ar([sig * wet, in * dry]);
				Out.ar(out, sig);
			},
			name: \freeze,
			addaction: \addAfter
		);
	}

}

+ PedalBoy {
	*delay{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\delay -> MappableArg.new(
					symbol: \delay,
					bounds: 0@4,
					default_value: 0.5,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\decay -> MappableArg.new(
					symbol: \decay,
					bounds: -4@4,
					default_value: 0,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(0.5),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(1),
			]),
			ugen_func: {
				arg in, out, wet, dry, delay, decay;
				var sig;
				in = In.ar(in);
				sig = CombC.ar(in, 4, delay, decay);
				sig = Mix.ar([sig * wet, dry * in]);
				ReplaceOut.ar(out, sig);
			},
			name: \delay,
			addaction: \addAfter
		);
	}

	*freeverb{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(0.5),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(1),
				\room -> MappableArg.new(
					symbol: \room,
					bounds: 0@1,
					default_value: 0.8,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\damp -> MappableArg.new(
					symbol: \damp,
					bounds: 0@1,
					default_value: 0.5,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
			]),
			ugen_func: {
				arg in, out, wet, dry, room, damp;
				var sig;

				in = In.ar(in);

				sig = FreeVerb.ar(
					in: in,
					mix: 1,
					room: room,
					damp: damp);

				sig = Mix.ar([sig * wet, in * dry]);
				ReplaceOut.ar(out, sig);

			},
			name: \freeverb,
			addaction: \addAfter
		)
	}
}

