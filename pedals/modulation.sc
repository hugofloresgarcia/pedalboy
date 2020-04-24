Modulator : Pedal {
	var
	<>min,
	<>max,
	<>oscillator;

	*newWithUgen{|parent, argument, ugen|

		var min = parent.mappable_args[argument].bounds.x;
		var max = parent.mappable_args[argument].bounds.y;
		var bus = parent.mappable_args[argument].bus;
		var default = parent.mappable_args[argument].default_value;
		if(parent.isNil, {
			"YOU DIDN'T SPECIFY A PARENT DUMBASS".postln;
		});
		if(parent.node.isNil, {
			"Synth must be playing".postln;
		});
		^super.new(
			parent.server,
			bus,
			bus,
			parent.node
		).modinit(min, max, ugen);
	}

	modinit{|min, max, ugen|
		this.min = min;
		this.max = max;
		this.oscillator = ugen;
		super.init(this.server, this.in, this.out, this.group);
	}

	*sine{|parent, argument|
		^this.newWithUgen(parent, argument, SinOsc)
	}

	*tri{|parent, argument|
		^this.newWithUgen(parent, argument, LFTri)
	}

	*pulse{|parent, argument|
		^this.newWithUgen(parent, argument, LFPulse)
	}

	*noise{|parent, argument|
		^this.newWithUgen(parent, argument, LFNoise0)
	}


	set_synth_params{
		this.addaction = \addBefore;
		this.synthdef = \mod;

		this.arg_dict = Dictionary.with(*[
			\in -> this.in,
			\out -> this.out]);

		this.set_mappable_args(Dictionary.with(*[
			\min -> MappableArg(
				symbol: \min,
				bounds: this.min@this.max,
				default_value: this.min,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\max -> MappableArg(
				symbol: \max,
				bounds: this.min@this.max,
				default_value: this.max,
				warp: \lin,
				gui_object: \knob,
				bus: Bus.control(this.server, 1)),
			\freq -> MappableArg(
				symbol: \freq,
				bounds: 0.01@1e3,
				default_value: 1,
				warp: \exp,
				gui_object: \knob,
				bus: Bus.control(this.server, 1))
		]));

		if (this.oscillator.isNil.not, {
			this.ugen_func = {
				arg in, out, min, max, freq;
				var sig;
				sig = this.oscillator.kr(freq: freq).range(min, max);
				ReplaceOut.kr(out, sig);
			}
		});
	}
}
