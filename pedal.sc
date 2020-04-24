Pedal {
	// abstract class for all pedals
	classvar
	<>num_active = 0;

	var
	<>server,
	<>in,
	<>out,

	<>synthdef,
	<>node,
	<>synth,
	<>ugen_func,
	<>addaction,
	<>group,

	<>arg_dict,
	<>mappable_args,

	<>gui_objs,
	<>view,
	<>flow;

	*new{|server, in, out, group|
		^super.new.init(server, in, out, group);
	}

	*from_synth_params{|server, in, out, group, mappable_arg_dict,
		ugen_func, name = \pedal, addaction = \addToTail|

		^Pedal.new(server, in, out, group).synth_param_init(mappable_arg_dict, ugen_func, name, addaction);
	}

	init{|server, in, out, group|
		num_active = num_active + 1;
		this.server = server;
		this.in = in;
		this.out = out;
		this.group = group;
		this.gui_objs = List.new();

		if(this.arg_dict.isNil, {
			this.arg_dict = Dictionary.new();
		});
		this.arg_dict[\in] = this.in;
		this.arg_dict[\out] = this.out;
	}

	synth_param_init{|mappable_arg_dict, ugen_func, name, addaction|
		this.synthdef = name;
		this.addaction = addaction;
		this.set_mappable_args(mappable_arg_dict);
		this.ugen_func = ugen_func;
		this.create_synthdef;
	}

	create_synthdef{
		if (this.ugen_func.isNil.not, {
			this.synth = SynthDef(this.synthdef, this.ugen_func).add;
		});
	}

	set_mappable_args{|dictionary|
		this.mappable_args = dictionary;
		this.mappable_args.do({
			arg m_arg;
			this.arg_dict.add(
				m_arg.symbol -> m_arg.as_synth_arg
			)
		});
	}

	make_synthdef{
		this.set_synth_params();
		if (this.ugen_func.isNil.not, {
			this.synth = SynthDef(this.synthdef, this.ugen_func).add;
		});
	}

	on{
		// this.synth = Synth(this.synthdef, this.arg_dict.asPairs, this.group, this.addaction);
		this.node = this.synth.play(this.group, this.arg_dict.asPairs, this.addaction);
	}

	get_bus{|argument|
		//return the control bus associated with an argument
		^this.mappable_args[argument].bus;
	}


	set_ugen_func{|func|
		this.ugen_func = func;
	}

	scope{|argument|
		this.get_bus(argument).scope;
	}

	connect{|other|
		if(other == nil, {
			this.out = 0;
		},{
			this.out = other.in;
		});
	}

	make_view{|parent, bounds|
		if(bounds.isKindOf(Point), {
			"ERROR: you cant put a point in these bounds"
		});
		this.view = View(parent, bounds)
		.background_(Color.rand(0.85, 0.95));

		this.flow = this.view.addFlowLayout(5@5, 5@5);

		StaticText(
			parent: this.view,
			bounds: (bounds.width - 10)@20)
		.string_(this.synthdef.asString.toUpper)
		.align_(\center)
		.background_(Color.rand(0.85, 0.95))
		.stringColor_(Color.rand(0.15, 0.25));
		this.flow.nextLine;

		this.add_gui_controls;
	}


	add_gui_controls{
		this.mappable_args.do({
			arg m_arg;
			var classnames = Dictionary.with(*[
				\knob -> EZKnob,
				\slider -> EZSlider,
				// \listview -> EZListView,
				\number-> EZNumber]);
			// this.gui_objs.add({
				classnames[m_arg.gui_object].new(
					parent: this.view,
					bounds: 40@80,
					label: m_arg.symbol.asString.toUpper,
					controlSpec: m_arg.control_spec,
					action: {
						arg v;
						m_arg.bus.set(v.value);},
					initVal: m_arg.default_value,
					initAction: true,
					layout: {
					var result;
					if(classnames[m_arg.gui_object] == EZSlider,{result = \horz}, {result = \vert});
					result;
					}.value
				)
				.setColors(
					stringBackground: Color.rand(0.85, 0.95),
					stringColor: Color.rand(0.05, 0.15),
					numBackground: Color.rand(0.85, 0.95),
					numStringColor: Color.rand(0.05, 0.15),
					numNormalColor: Color.rand(0.05, 0.15)
				)
				.font_(Font("Helvetica", 9));
		// });
		});
	}



	*directory{
		var all = Dictionary.with(*[
			\input_buffer -> Pedal.input_buffer(),
			\output_buffer -> Pedal.output_buffer(),
			\panner -> Pedal.panner(),
			\grain_pitch_shifter -> Pedal.grain_pitch_shifter(),
			\pitch_follower -> Pedal.pitch_follower(),
			\mono_pitch -> Pedal.mono_pitch(),
			\saw_synth -> Pedal.saw_synth(),
			\tri_synth -> Pedal.tri_synth(),
			\sine_synth -> Pedal.tri_synth()
		]);
		all.keysValuesDo({
			arg key, value;
			key.postln;
		});
		^all
	}
}
