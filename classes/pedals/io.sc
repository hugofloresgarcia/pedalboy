+PedalBoy{

	*input_buffer{|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1))
			]),
			ugen_func: {
				arg in = 0, out = 0, gain = 1;
				var sig;

				sig = SoundIn.ar(in);

				ReplaceOut.ar(out, sig * gain);
			},
			name: \input_buffer,
			addaction: \addToHead
		);
	}

	*output_buffer{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1));
			]),
			ugen_func: {
				arg in = 0, out = 0, gain=1;
				var sig;
				sig = In.ar(in);
				ReplaceOut.ar(out, sig * gain)
			},
			name: \output_buffer,
			addaction: \addAfter);
	}

	*panner{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\pos -> MappableArg.pos(Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, pos;
				var sig;
				sig = In.ar(in);
				sig = Pan2.ar(sig, pos);
				ReplaceOut.ar(out, sig)
			},
			name: \panner,
			addaction: \addAfter);
	}
}