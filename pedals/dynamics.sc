+ Pedal {
	*compressor{
		|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[

				\thresh -> MappableArg.new(
					symbol: \thresh,
					bounds: 0@1,
					default_value: 0.8,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\ratio -> MappableArg.new(
					symbol: \ratio,
					bounds: 0.1@20,
					default_value: 5,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\atk -> MappableArg.atk(Bus.control(server, 1)),
				\rel -> MappableArg.rel(Bus.control(server, 1)),
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(1),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(0.1),
				\makeup -> MappableArg.new(
					symbol: \makeup,
					bounds: 0.01@2,
					default_value: 1,
					warp: \lin,
					gui_object: \knob,
					bus: (Bus.control(server, 1)))

			]),
			ugen_func: {
				arg in, out, thresh, ratio, atk, rel, wet, dry, makeup;
				var sig;

				in = In.ar(in);
				sig = Compander.ar(
					in: in,
					control: in,
					thresh: thresh,
					slopeAbove: 1/ratio,
					clampTime: atk,
					relaxTime: rel,
					mul: makeup
				);

				sig = Mix.ar([sig * wet, in * dry]);
				ReplaceOut.ar(out, sig);
			},
			name: \compressor,
			addaction: \addAfter
		);
	}
}