+ Pedal {
	*grain_pitch_shifter{
		|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\interval -> MappableArg(
					symbol: \interval,
					bounds: -24@24,
					default_value: 0,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\window_size -> MappableArg(
					symbol: \window_size,
					bounds: 0.001@0.1,
					default_value: 0.01,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(
					bus: Bus.control(server, 1)
				),
				\dry -> MappableArg.dry(
					bus: Bus.control(server, 1)
				),
			]),
			ugen_func: {
				arg in = 0, out = 0, interval = 0,
				window_size = 0.01,
				pitch_dispersion = 0, time_dispersion = 0,
				wet = 0.5, dry =0.5;
				var sig, dry_sig;
				sig = In.ar(in, 1);
				dry_sig = sig;

				sig = PitchShift.ar(
					in:sig,
					windowSize: window_size,
					pitchRatio: interval.midiratio,
					pitchDispersion: pitch_dispersion,
					timeDispersion: time_dispersion
				);
				sig = Mix.ar([wet * sig, dry * dry_sig]);
				// sig.scope;
				ReplaceOut.ar(out, sig);
			},
			name: \grain_pitch_shifter,
			addaction: \addToTail);
	}

	*pitch_follower{
		|server, in, out, group|
		^Pedal.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.new(),
			ugen_func: {
				arg in = 0, out = 0;
				var amp, freq, hasFreq, sound;

				in = In.ar(in, 1);

				amp = Amplitude.kr(
					in: in,
					attackTime: 0.05,
					releaseTime: 0.05);

				# freq, hasFreq = Pitch.kr(
					in: in,
					ampThreshold: 0.02,
					median: 7);

				sound = CombC.ar(
					in: LPF.ar(in, 1000),
					maxdelaytime: 0.1,
					delaytime: (2 * freq + 10).reciprocal,
					decaytime: -6
				).distort * 0.05;

				6.do({
					sound = AllpassN.ar(
						in: sound,
						maxdelaytime: 0.040,
						delaytime: [0.040.rand, 0.040.rand],
						decaytime: 2)
				});

				ReplaceOut.ar(out, sound);
			},
			name: \pitch_follower,
			addaction: \addToTail);
	}


	*mono_pitch{
		|server, in, out, group|
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
				)
			]),
			ugen_func: {
				arg in, out, interval = 12, wet = 1, dry = 1;
				var freq, hasFreq, sig;
				in = In.ar(in);
				# freq, hasFreq = Pitch.kr(in);
				sig = Mix.ar([
					SinOsc.ar(freq * interval.midiratio) * in * wet,
					in * dry]);
				Out.ar(out, sig);
			},
			name: \mono_pitch,
			addaction: \addToTail);
	}
}
