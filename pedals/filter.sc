 + PedalBoy{
	*env_filter{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1)),
				\cutoff -> MappableArg.cutoff(Bus.control(server, 1)),
				\rq -> MappableArg.new(
					symbol: \rq,
					bounds: 0.01@1,
					default_value: 1.0,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(Bus.control(server, 1)),
				\dry -> MappableArg.dry(Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, gain, cutoff, rq, wet, dry;
				var sig, env;

				in = In.ar(in) * gain;

				env = EnvFollow.kr(in);

				sig = RLPF.ar(
					in: in,
					freq: cutoff + (20e3 - cutoff)* env,
					rq: rq);

				sig = Mix.ar([sig * wet, dry * in]);
				ReplaceOut.ar(out, sig);

			},
			name: \env_filter,
			addaction: \addAfter
		);
	}
}