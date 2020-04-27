Modulator : PedalBoy {
	var
	<>min,
	<>max,
	<>oscillator,
	<>parent;

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
		).modinit(min, max, ugen, parent);
	}

	modinit{|min, max, ugen, parent|
		this.parent = parent;
		this.min = min;
		this.max = max;
		this.oscillator = ugen;
		super.init(this.server, this.in, this.out, this.group).make_synthdef;
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
		this.synthdef = (\mod.asString ++ "_" ++ this.parent.synthdef.asString).asSymbol;
		this.scope_bus = Bus.control(this.server, 1);

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



	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	// this is where the scope view is created
	make_scope_view{

		this.scope_view = View(this.view, Rect(0, 0, 185, 80));

		// set size bounds for VLayout compatibility
		this.scope_view.minSize_(Size(this.scope_view.bounds.width,
			this.scope_view.bounds.height));
		this.scope_view.maxSize_(Size(this.scope_view.bounds.width,
			this.scope_view.bounds.height));



		// this.server.sync;
		PedalBoyScope(this.server, 1, this.scope_bus.index, 1024, 1, 'control', this.scope_view)
		.index_(this.scope_bus.index)
		.view.children[0]
		.style_(0)
		.fill_(true)
		.y_(-0.5)
		.yZoom_(8)
		.waveColors_([Color.new(0.5.rrand(0.85),0.5.rrand(0.85), 0.5.rrand(0.85))])
		.focus
		.start;
	}

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	// this is where the scope view is created
	scope{
		var out;
		// this.synth_node.postln
		this.out.postln;

		if (this.out.isKindOf(Bus).not, {
			out = this.out;
		}, {
			out = this.out.index;
		});

		this.scope_node = SynthDef((this.synthdef.asString ++ "_scope").asSymbol,
			{arg in, out;
				var sig;
				var mid = (this.max - this.min)/2;
				sig = In.kr(in, 1) - mid;
				// sig = sig - (this.max - this.min);
				sig = sig * 0.5;

				Out.kr(out, sig);
		}).play(this.synth_node, [\in, out, \out, this.scope_bus.index], \addAfter);
	}
}

+ PedalBoy {

	*vibrato{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\depth -> MappableArg.new(
					symbol: \depth,
					bounds: 0@1,
					default_value: 0.5,
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
				\wet -> MappableArg.wet(Bus.control(server, 1)).default_value_(0.9),
				\dry -> MappableArg.dry(Bus.control(server, 1)).default_value_(0.01),
			]),
			ugen_func: {
				arg in, out, wet, dry, depth, speed;
				var sig;
				in = In.ar(in);

				sig = DelayC.ar(
					in: in,
					maxdelaytime: 0.2,
					delaytime: SinOsc.ar(speed).range(0, 0.02) * depth);

				sig = Mix.ar([sig * wet, dry * in]);
				ReplaceOut.ar(out, sig);
			},
			name: \vibrato,
			addaction: \addAfter,
		);
	}

	*chorus{
		|server, in, out, group|
		^PedalBoy.from_synth_params(
			server: server,
			in: in,
			out: out,
			group: group,
			mappable_arg_dict: Dictionary.with(*[
				\depth -> MappableArg.new(
					symbol: \depth,
					bounds: 0@1,
					default_value: 0.5,
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
				\wet -> MappableArg.wet(Bus.control(server, 1)),
				\dry -> MappableArg.dry(Bus.control(server, 1))
			]),
			ugen_func: {
				arg in, out, wet, dry, depth, speed;
				var sig;
				var n = 10;

				in = In.ar(in);

				sig = Mix.fill(n, {

					var maxdelaytime= rrand(0.01,0.03);

					var half= maxdelaytime*0.5;

					var quarter= maxdelaytime*0.25;



					//%half+(quarter*LPF.ar(WhiteNoise.ar,rrand(1.0,10)))

					DelayC.ar(in, maxdelaytime, LFNoise1.kr(Rand(5,10),0.01,0.02) )
				});




				sig = Mix.ar([sig * wet, dry * in]);
				ReplaceOut.ar(out, sig);
			},
			name: \chorus,
			addaction: \addAfter,
		);
	}

}
