+ Pedal {
	*delay{
		|server, in, out, group|
		^Pedal.from_synth_params(
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
				\wet -> MappableArg.wet(Bus.control(server, 1)),
				\dry -> MappableArg.dry(Bus.control(server, 1))
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
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\wet -> MappableArg.wet(Bus.control(server, 1)),
				\dry -> MappableArg.dry(Bus.control(server, 1)),
				\room -> MappableArg.new(
					symbol: \room,
					bounds: 0@1,
					default_value: 0.5,
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

				