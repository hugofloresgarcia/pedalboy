+ PedalBoy {
	*compressor{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
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
	*vinyl_boy{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[

				\thresh -> MappableArg.new(
					symbol: \thresh,
					bounds: 0@1,
					default_value: 0.3,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\ratio -> MappableArg.new(
					symbol: \ratio,
					bounds: 0.1@1000,
					default_value: 5,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(1),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(0.1),
				\depth -> MappableArg.new(
					symbol: \depth,
					bounds: 0@1,
					default_value: 0.15,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\speed -> MappableArg.new(
					symbol: \speed,
					bounds: 0@1,
					default_value: 0.5,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\makeup -> MappableArg.new(
					symbol: \makeup,
					bounds: 0.01@2,
					default_value: 1,
					warp: \lin,
					gui_object: \knob,
					bus: (Bus.control(server, 1))),
				\density -> MappableArg.new(
					symbol: \density,
					bounds: 1@200,
					default_value: 10,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\dust -> MappableArg.new(
					symbol: \dust,
					bounds: 0.01@1,
					default_value: 0.03,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
			]),
			ugen_func: {
				arg in, out, thresh, ratio, atk = 0.1, rel = 1, wet, dry, makeup, depth, speed, density, dust;
				var sig;

				in = In.ar(in);
				sig = Compander.ar(
					in: in,
					control: in,
					thresh: thresh,
					slopeAbove: 1/ratio,
					clampTime: atk,
					relaxTime: rel,
					mul: makeup);

				sig = DelayC.ar(
					in: sig,
					maxdelaytime: 0.2,
					delaytime: SinOsc.ar(speed).range(0, 0.02) * depth);

				sig = Mix.ar([
					sig ,
					MoogFF.ar(
						in: Dust.ar(
							density: density,
							mul: dust
						),
						freq: LFNoise0.ar(3).range(300, 800),
						gain: 1),
					WhiteNoise.ar(0.001) * dust,
				]);

				sig = Mix.ar([sig * wet, in * dry]);
				ReplaceOut.ar(out, sig);
			},
			name: \vinyl_boy,
			addaction: \addAfter
		);
	}
}