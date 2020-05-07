+ PedalBoy {
	*bitcrusher{|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1)).default_value_(1),
				\rq -> MappableArg.new(
					symbol: \rq,
					bounds: 0.1@1,
					default_value: 0.3,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(1),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(0),
				\min -> MappableArg.new(
					symbol: \min,
					bounds: 40@16e3 ,
					default_value: 100,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\ctrl -> MappableArg.new(
					symbol: \ctrl,
					bounds: 0.001@1 ,
					default_value: 0,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)).default_value_(0.8),
				\res -> MappableArg.new(
					symbol: \res,
					bounds: 10@100 ,
					default_value: 30,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\max -> MappableArg.new(
					symbol: \max,
					bounds: 40@20e3,
					default_value: 20e3,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, gain, rq, wet, dry, min, max, res, ctrl;
				var sig, env, cutoff;

				in = In.ar(in);

				ctrl = Lag.kr(ctrl, 0.1);

				sig = (in * gain* res).round / res;

				cutoff = ctrl.linexp(0, 1, min, max).clip(100, 20e3);

				sig = RLPF.ar(
					in: sig,
					freq: cutoff,
					rq: rq);

				sig = Mix.ar([sig * wet, dry * in]);
				ReplaceOut.ar(out, sig);
			},
			name: \bitcrusher,
			addaction: \addAfter
		);
	}

	*soft_fuzz{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1)).default_value_(1),
				\tone -> MappableArg.new(
					symbol: \tone,
					bounds: 0.01@1,
					default_value: 0.1,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\res -> MappableArg.new(
					symbol: \res,
					bounds: 0@2,
					default_value: 1.37,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\level -> MappableArg.new(
					symbol: \level,
					bounds: 0@1,
					default_value: 0.25,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, gain, tone, res, level;
				var sig, sign;

				in  = In.ar(in, 1);

				in = in * gain * 100;


				sig = in.softclip;

				sig = MoogFF.ar(
					in: sig,
					freq: (100 +  (18e3*tone)),
					gain: res);

				sig = sig * level;

				ReplaceOut.ar(out, sig);
			},
			name: \soft_fuzz,
			addaction: \addAfter,
		);
	}

	*futh{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\gain -> MappableArg.gain(Bus.control(server, 1)).default_value_(1),
				\tone -> MappableArg.new(
					symbol: \tone,
					bounds: 0.01@1,
					default_value: 0.1,
					warp: \exp,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\res -> MappableArg.new(
					symbol: \res,
					bounds: 0@2,
					default_value: 1.37,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1)),
				\level -> MappableArg.new(
					symbol: \level,
					bounds: 0@1,
					default_value: 0.25,
					warp: \lin,
					gui_object: \knob,
					bus: Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, gain, tone, res, level;
				var sig, sign;

				in  = In.ar(in, 1);

				in = in * gain * 100;


				sig = in.softclip;

				sig = in.clip(-0.5, 0.5);

				sig = MoogFF.ar(
					in: sig,
					freq: (100 +  (18e3*tone)),
					gain: res);

				sig = sig * level;

				ReplaceOut.ar(out, sig);
			},
			name: \futh,
			addaction: \addAfter,
		);
	}
}