 + Pedal{
	*env_filter{
		|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1)),
				\cutoff -> MappableArg.cutoff(Bus.control(server, 1)),
				\rq -> MappableArg.new(
					symbol: \rq,
					bounds: 0@1,
					default_value: 0.1,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, gain, cutoff, rq;
				var sig, env;

				in = In.ar(in);

				env = EnvFollo

			}
		);
	}
}