
+ Pedal{
	*saw_synth{|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict:  Dictionary.with(*[
				\interval -> MappableArg.new(
					symbol: \interval,
					bounds: -24@24,
					default_value: 12,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(
					bus: Bus.control(server, 1)
				),
				\dry -> MappableArg.dry(
					bus: Bus.control(server, 1)
				),
				\cutoff -> MappableArg.cutoff(
					bus: Bus.control(server, 1),
				),
				\atk -> MappableArg.atk(
					bus: Bus.control(server, 1),
				),
				\rel -> MappableArg.rel(
					bus: Bus.control(server, 1),
				)
			]),
			ugen_func: {
				arg in, out, interval = 12, wet = 1, dry = 1, cutoff = 3e3, atk = 0.1, rel = 0.1;
				var freq, hasFreq, sig, env;
				in = In.ar(in);


				# freq, hasFreq = Pitch.kr(in);

				env = EnvGen.ar(
					envelope: Env.adsr(atk,releaseTime: rel),
					gate: hasFreq,
					doneAction: 0);


				freq = freq * interval.midiratio;

				sig = LFTri.ar(
					freq: freq + (LFNoise0.kr(freq, freq/20)),
					mul: 0.8 + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: 2);

				sig = Mix.ar([sig * hasFreq * env * wet, in * dry]);
				ReplaceOut.ar(out, sig);
			},
			name: \saw_synth,
			addaction: \addAfter
		);
	}


	*tri_synth{|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\interval -> MappableArg.new(
					symbol: \interval,
					bounds: -24@24,
					default_value: 12,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(
					bus: Bus.control(server, 1)
				),
				\dry -> MappableArg.dry(
					bus: Bus.control(server, 1)
				),
				\cutoff -> MappableArg.cutoff(
					bus: Bus.control(server, 1),
				),
				\atk -> MappableArg.atk(
					bus: Bus.control(server, 1),
				),
				\rel -> MappableArg.rel(
					bus: Bus.control(server, 1),
				)
			]),
			ugen_func: {
				arg in, out, interval = 12, wet = 1, dry = 1, cutoff = 3e3, atk = 0.1;
				var freq, hasFreq, sig;
				in = In.ar(in);

				# freq, hasFreq = Pitch.kr(in);

				freq = freq * interval.midiratio;

				sig = Saw.ar(
					freq: (freq + (LFNoise0.kr(freq, freq/30))),
					mul: 0.8 +
					LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2))!10;

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: 2);

				sig = Mix.ar([sig * hasFreq * wet, in * dry]);
				ReplaceOut.ar(out, sig);
			},
			name: \tri_synth,
			addaction: \addAfter
		);
	}


	*sine_synth{|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\interval -> MappableArg.new(
					symbol: \interval,
					bounds: -24@24,
					default_value: 12,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(
					bus: Bus.control(server, 1)
				),
				\dry -> MappableArg.dry(
					bus: Bus.control(server, 1)
				),
				\cutoff -> MappableArg.cutoff(
					bus: Bus.control(server, 1),
				),
				\atk -> MappableArg.atk(
					bus: Bus.control(server, 1),
				),
				\rel -> MappableArg.rel(
					bus: Bus.control(server, 1),
				)
			]),
			ugen_func: {
				arg in, out, interval = 12, wet = 1, dry = 1, cutoff = 3e3, atk = 0.1, rel = 0.1;
				var freq, hasFreq, sig,amp;

				in = In.ar(in);

				# freq, hasFreq = Pitch.kr(in);

				sig = SinOsc.ar(freq * interval.midiratio);

				sig = MoogFF.ar(
					in: sig,
					freq: cutoff,
					gain: 2);

				sig = Mix.ar([sig * hasFreq * wet , in * dry]);
				ReplaceOut.ar(out, sig);
			},
			name: \sine_synth,
			addaction: \addAfter
		);
	}


}