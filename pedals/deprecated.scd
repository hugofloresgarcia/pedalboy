
InputBuffer : Pedal {
	set_synth_params{
		this.addaction = \addToHead;
		this.synthdef = \input_buffer;

		this.arg_dict = Dictionary.with(*[
			\in -> this.in,
			\out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\gain -> MappableArg.gain(Bus.control(this.server, 1));
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, gain;
			var sig;

			sig = SoundIn.ar(in);

			ReplaceOut.ar(out, sig * gain);
		}
	}
}

Panner : Pedal {
	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \panner;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\pos -> MappableArg(
				symbol: \pos,
				bounds: -1@1.0,
				default_value: 0,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1))
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, pos;
			var sig;
			sig = In.ar(in);
			sig = Pan2.ar(sig, pos);
			ReplaceOut.ar(out, sig)
		};
	}
}


OutputBuffer : Pedal {
	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \output_buffer;

		this.arg_dict = Dictionary.with(*[
			\in -> this.in,
			\out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\gain -> MappableArg.gain(Bus.control(this.server, 1));
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, gain;
			var sig;
			sig = In.ar(in);
			ReplaceOut.ar(out, sig * gain)
		};
	}
}


GrainPitchShifter : Pedal {
	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \grain_pitch_shift;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\interval -> MappableArg(
				symbol: \interval,
				bounds: -24@24,
				default_value: 0,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\window_size -> MappableArg(
				symbol: \window_size,
				bounds: 0.001@0.1,
				default_value: 0.01,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\wet -> MappableArg.wet(
				bus: Bus.control(this.server, 1)
			),
			\dry -> MappableArg.dry(
				bus: Bus.control(this.server, 1)
			),
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
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
		}
	}
}


PitchFollower : Pedal{

	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \pitch_shift;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[

		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
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
		}
	}
}



MonoPitch : Pedal{

	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \mono_pitch;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\interval -> MappableArg.new(
				symbol: \interval,
				bounds: -24@24,
				default_value: 12,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\wet -> MappableArg.wet(
				bus: Bus.control(this.server, 1)
			),
			\dry -> MappableArg.dry(
				bus: Bus.control(this.server, 1)
			)
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, interval = 12, wet = 1, dry = 1;
            var freq, hasFreq, sig;
            in = In.ar(in);
            # freq, hasFreq = Pitch.kr(in);
			sig = Mix.ar([
				SinOsc.ar(freq * interval.midiratio) * in * wet,
				in * dry]);
            Out.ar(out, sig);
        }

	}
}



LoFiSaw : Pedal{

	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \lofi_saw;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\interval -> MappableArg.new(
				symbol: \interval,
				bounds: -24@24,
				default_value: 12,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\wet -> MappableArg.wet(
				bus: Bus.control(this.server, 1)
			),
			\dry -> MappableArg.dry(
				bus: Bus.control(this.server, 1)
			),
			\cutoff -> MappableArg.cutoff(
				bus: Bus.control(this.server, 1),
			),
			\atk -> MappableArg.atk(
				bus: Bus.control(this.server, 1),
			),
			\rel -> MappableArg.rel(
				bus: Bus.control(this.server, 1),
			)
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, interval = 12, wet = 1, dry = 1, cutoff = 3e3, atk = 0.1, rel = 0.1;
            var freq, hasFreq, sig;
            in = In.ar(in);

			# freq, hasFreq = Pitch.kr(in);

			freq = freq * interval.midiratio;

			sig = LFTri.ar(
				freq: freq + (LFNoise0.kr(freq, freq/20)),
				mul: 0.8 + LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2)).distort;

			sig = MoogFF.ar(
				in: sig,
				freq: cutoff,
				gain: 2);

			sig = Mix.ar([sig * hasFreq * wet, in * dry]);
            Out.ar(out, sig);
        }

	}
}



LoFiTri : Pedal{

	set_synth_params{
		this.addaction = \addToTail;
		this.synthdef = \lofi_saw;

		this.arg_dict = Dictionary.with(*[
			in -> this.in,
			out -> this.out]);

		this.mappable_args = Dictionary.with(*[
			\interval -> MappableArg.new(
				symbol: \interval,
				bounds: -24@24,
				default_value: 12,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\wet -> MappableArg.wet(
				bus: Bus.control(this.server, 1)
			),
			\dry -> MappableArg.dry(
				bus: Bus.control(this.server, 1)
			),
			\cutoff -> MappableArg.cutoff(
				bus: Bus.control(this.server, 1);
			),
			\atk -> MappableArg.atk(
				bus: Bus.control(this.server, 1),
			),
			\rel -> MappableArg.rel(
				bus: Bus.control(this.server, 1),
			)
		]);

		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});

		this.ugen_func = {
			arg in, out, interval = 12, wet = 1, dry = 1, cutoff = 3e3, atk = 0.1;
            var freq, hasFreq, sig;
            in = In.ar(in);

			# freq, hasFreq = Pitch.kr(in);

			freq = freq * interval.midiratio;

			sig = Saw.ar(
				freq: (freq + (LFNoise0.kr(freq, freq/20))),
				mul: 0.8 +
				LFNoise0.kr(Rand(1, 5)).range(-0.2, 0.2))!10;

			sig = MoogFF.ar(
				in: sig,
				freq: cutoff,
				gain: 2);

			sig = Mix.ar([sig * hasFreq * wet, in * dry]);
            Out.ar(out, sig);
        }

	}
}
