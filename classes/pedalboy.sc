PedalBoy {
	classvar
	<>num_active = 0,
	<>all;

	var
	<>instance_id,
	<>server,
	<>in,
	<>out,
	<>parent,

	<>mididef_dict,

	<>synthdef,
	<>synth_node,
	<>synth,
	<>ugen_func,
	<>addaction,
	<>group,

	<>arg_dict,
	<>mappable_args,

	<>gui_objs,
	<>master_bounds,
	<>knobs,

	<>scope_view,
	<>control_view,
	<>label_view,
	<>button_view,
	<>bypass_button,

	<>focused_color,
	<>normal_color,

	<>view,
	<>flow,

	<>scope_node,
	<>scope_bus;

	*new{|server, in, out, group|
		server = server ? Server.default;
		if(server.hasBooted.not, {
			Error("server has not been booted").throw;
		});
		^super.new.init(server, in, out, group);

	}

	*from_synth_params{|server, in, out, group, mappable_arg_dict,
		ugen_func, name = \pedal, addaction = \addToTail|

		^this.new(server, in, out, group).synth_param_init(mappable_arg_dict, ugen_func, name, addaction);
	}

	init{|server, in, out, group|
		num_active = num_active + 1;
		this.instance_id = num_active;
		this.server = server;
		this.in = in;
		this.out = out;
		this.group = group;
		this.gui_objs = List.new();

		this.scope_bus = this.scope_bus ? Bus.audio(this.server, 1);

		if(server.isNil.not, {
			NodeWatcher.newFrom(server);
		});

		if(this.arg_dict.isNil, {
			this.arg_dict = Dictionary.new();
			this.mididef_dict = Dictionary.new();
		});
		this.arg_dict[\in] = this.in;
		this.arg_dict[\out] = this.out;
	}

	is_bypassed{
		^this.node.isRunning.not;
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

	gui_bypass{
		this.bypass_button.valueAction_(1);
	}

	// listen_midi{
	// 	var result = Bus.control(this.server, 1);
	// 	var return;
	//
	// 	var mididef = MIDIdef.new(\listener, {
	// 		arg val, num;
	// 		result.set(num);
	// 	})
	// 	while(result.get.isNil,{
	//
	// 	});
	// 	return = result.get;
	// 	result.free;
	// 	mididef.free;
	// 	^result;
	// }


	assign_bypass{|midinote|
		var midi_key = ("bypass_" ++ this.synthdef.asString).asSymbol;
		this.mididef_dict.add(
			\bypass -> MIDIdef.noteOn(
				key: midi_key,
				func: {
					arg vel, note;
					Routine({
						if (note == midinote,{
							var val;
							if(this.bypass_button.value == 0, {val = 1}, {val = 0});
							this.bypass_button.valueAction_(val);
						});
					}).play(AppClock)
			});
		);
	}

	assign_knob{|ccNum, argument|
		var m_arg = this.mappable_args[argument];
		var midi_key = ("\knob_"++ argument.asString ++ this.synthdef.asString).asSymbol;

		this.mididef_dict.add(
			argument -> MIDIdef.cc(
				key: midi_key,
				func: {
					arg val;
					var m_arg = this.mappable_args[argument];
					var knob = this.knobs[m_arg.symbol];
					val = knob.controlSpec.map(val / 127);
					Routine({
						knob.valueAction_(val);
					}).play(AppClock)
			}, ccNum: ccNum);
		);

	}

	unassign_knob{|argument|
		this.mididef_dict.at(argument).free;
	}

	node{
		^this.scope_node;
	}


	on{
		//only create a new node on the server if there isn't already one
		if(this.node.isPlaying, {
			this.synth_node.run(true);
			this.scope_node.run(true); // creates the scope synthdef
			"resuming synth ".post; this.synthdef.postln;
		}, {
			"creating node ".post; this.synthdef.postln;
			this.synth_node = this.synth.play(this.group, this.arg_dict.asPairs, this.addaction);
			this.scope();
			this.node.register(true);
		});
	}

	bypass{
		Routine({
			this.server.sync;
			// if(this.node.isPlaying, {
				// "bypassing..".post; this.synthdef.postln;

			this.synth_node.run(false);
			this.scope_node.run(false);
		}).play;
	}

	free{
		this.synth_node.free;
		this.scope_node.free;
	}

	get_bus{|argument|
		//return the control bus associated with an argument
		^this.mappable_args[argument].bus;
	}

	set_bus{|argument, value|
		this.get_bus(argument).set(value);
	}

	set_ugen_func{|func|
		this.ugen_func = func;
	}

	scope_arg{|argument|
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
		this.master_bounds = 200@300;
		//towards scalable pedals. this was the original size used during prototyping,
		/// and the views will continue to be scaled accordingly.
		this.normal_color = Color.rand(0.75, 0.95);
		this.focused_color = this.normal_color.blend(Color.new255(51, 51, 51), 0.2);
		if(bounds.isKindOf(Point), {
			"ERROR: you cant put a point in these bounds"
		});
		this.view = View(parent, bounds)
		.background_(Color.rand(0.75, 0.95));

		this.view_label;
		this.make_scope_view;
		this.add_gui_controls;
		this.add_buttons;

		// "making view for ".post; this.synthdef.postln;

		this.view.layout_(
			VLayout(
				this.label_view,
				this.scope_view,
				this.control_view,
				this.bypass_button
			)
		);
	}

	view_label{
		var b = this.master_bounds;
		var a = this.view.bounds.extent;
		// this.view.layout.insert(
		this.label_view = View(
			parent: this.view,
			bounds: Rect(0, 0, (this.view.bounds.width - 15), 20 * a.y / b.y));
		this.label_view.minSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height));
		this.label_view.maxSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height));

		StaticText(
			parent: this.label_view,
			bounds: this.label_view.bounds)
		.string_(this.synthdef.asString.toUpper)
		.align_(\center)
		.background_(Color.rand(0.75, 0.95))
		.stringColor_(Color.rand(0.15, 0.25));
	}

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	// this is where the scope view is created
	make_scope_view{
		var b = this.master_bounds;
		var a = this.view.bounds.extent;

		this.scope_view = View(this.view, Rect(0, 0, 185*a.x/b.x, 80*a.y/b.y));

		// set size bounds for VLayout compatibility
		this.scope_view.minSize_(Size(this.scope_view.bounds.width,
			this.scope_view.bounds.height));
		this.scope_view.maxSize_(Size(this.scope_view.bounds.width,
			this.scope_view.bounds.height));



		// this.server.sync;
		PedalScope(this.server, 1, this.scope_bus.index, 1024, 1, 'audio', this.scope_view)
		.index_(this.scope_bus.index)
		.view.children[0]
		.style_(0)
		.fill_(true)
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
		// this.out.postln;

		if (this.out.isKindOf(Bus).not, {
			out = this.out;
		}, {
			out = this.out.index;
		});

		this.scope_node = SynthDef((this.synthdef.asString ++ "_scope").asSymbol,
			{arg in, out;
				var sig;
				sig = In.ar(in, 1);
				Out.ar(out, sig);
		}).play(this.synth_node, [\in, out, \out, this.scope_bus.index], \addAfter);
	}

	add_gui_controls{
		var b = this.master_bounds;
		var a = this.view.bounds.extent;
		this.knobs = Dictionary.new();
		this.control_view = FlowView(
			parent: this.view,
			bounds: Rect(0, 0, 200*a.x/b.x, 280*a.y/b.y),
			margin: 2@2,
			gap: 2@2,
		)
		.minSize_(Size(200*a.x/b.x, 150*a.y/b.y))
		.maxSize_(Size(200*a.x/b.x, 150*a.y/b.y));
		this.mappable_args.do({
			arg m_arg, count;
			if(m_arg.gui_object == \knob, {
				if((count.mod(4) == 0) && (count != 0), {
					this.control_view.decorator.nextLine;
				});
				this.knobs.add(m_arg.symbol -> EZKnob.new(
					parent: this.control_view,
					bounds: (43*a.x/b.x)@(70*a.y/b.y),
					label: m_arg.symbol.asString.toUpper,
					controlSpec: m_arg.control_spec,
					action: {
						arg v;
						m_arg.bus.set(v.value);},
					initVal: m_arg.bus.getSynchronous,
					initAction: true,
					labelWidth: 60 * a.x/b.x,
					labelHeight: 20 * a.y/b.y,
					layout: \vert
				)
				.setColors(
					stringBackground: Color.rand(0.75, 0.95),
					stringColor: Color.rand(0.05, 0.15),
					numBackground: Color.rand(0.75, 0.95),
					numStringColor: Color.rand(0.05, 0.15),
					numNormalColor: Color.rand(0.05, 0.15)
				)
				.font_(Font("Helvetica", 9));
				);
				this.knobs.at(m_arg.symbol).view.setContextMenuActions(
					MenuAction("assign knob", {
						EZNumber.new(
							parent: nil,
							bounds: 160@50,
							label: ("assign" + m_arg.symbol.asString + "(ccNum)"),
							controlSpec: ControlSpec(0, 127, \lin),
							action: {arg v; this.assign_knob(v.value.asInteger, m_arg.symbol) ; v.view.parent.close},
							initVal: 0,
							initAction: false)
						.alwaysOnTop_(true);
					}),
					MenuAction("unassign knob", {this.unassign_knob(m_arg.symbol)}),
				);
			});
			// });
		});
		// this.view.decorator.nextLine;
		// this.view.layout.add(gui_control_view);
	}


	add_buttons{
		var b = this.master_bounds;
		var a = this.view.bounds.extent;
		var bypass_button;

		this.bypass_button = Button.new(
			parent: this.view,
			bounds: Rect(0, 0, this.view.bounds.width-15, 20*a.y/b.y))
		.minSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height))
		.maxSize_(Size(this.label_view.bounds.width, this.label_view.bounds.height))
		.states_([
			["BYPASS", Color.new255(51, 51, 51), Color.new(0.9, 0.5, 0.5)],
			["ON", Color.new255(51, 51, 51), Color.new(0.5, 0.9, 0.5)]])
		.action_({
			arg button;
			button.value.switch(
				0, {this.on; button.background_(Color.new(0.9, 0.5, 0.5))},
				1, {this.bypass; button.background_(Color.new(0.5, 0.9, 0.5))});

		});
		this.node.isPlaying.if({
			Routine({
				this.server.sync;
				"checking node ".post; this.synthdef; " for bypass".postln;
				"is bypassed: ".post; this.is_bypassed.postln;
				this.bypass_button.valueAction_(this.is_bypassed.asInteger);
			}).play(AppClock);
		});

		// this.view.layout.add(bypass_button);
	}


	*make_dir{
		this.all = Dictionary.with(*[
			\input_buffer -> { PedalBoy.input_buffer()},
			\output_buffer -> { PedalBoy.output_buffer()},
			\panner -> { PedalBoy.panner()},
			\grain_pitch_shifter -> { PedalBoy.grain_pitch_shifter()},
			\pitch_follower -> { PedalBoy.pitch_follower()},
			\pitch_shift -> { PedalBoy.pitch_shift()},
			\saw_synth -> { PedalBoy.saw_synth()},
			\tri_synth -> { PedalBoy.tri_synth()},
			\sine_synth -> { PedalBoy.tri_synth()},
			\fm_synth -> { PedalBoy.fm_synth()},
			\delay -> { PedalBoy.delay()},
			\compressor -> { PedalBoy.compressor()},
			\freeverb -> { PedalBoy.freeverb()},
			\env_filter -> { PedalBoy.env_filter()},
			\vibrato -> { PedalBoy.vibrato()},
			\shibrato -> { PedalBoy.shibrato()},
			\vinyl_boy -> { PedalBoy.vinyl_boy()},
			\looper_boy -> { LooperBoy.looper()},
			\grain_looper -> { GrainLooper.looper()},
			// \chorus -> { PedalBoy.chorus()},
			\env_filter -> { PedalBoy.env_filter()},
			\wah -> { PedalBoy.wah()},
			\bitcrusher -> { PedalBoy.bitcrusher()},
			\g_hex -> { PedalBoy.g_hex()},
			\futh -> { PedalBoy.futh()},
			\soft_fuzz -> { PedalBoy.soft_fuzz()},
		]);
	}

	*directory{
		this.all.keysValuesDo({
			arg key, value;
			key.postln;
		});
		^this.all
	}

	set_all_knobs{|knob_value_list|
		this.control_view.children.do({
			arg ezknob, index;
			ezknob.children[1].valueAction_(knob_value_list[index]);
		});
	}

	freeze_knobs{
		var knob_values = List.new();
		this.control_view.children.do({
			arg ezknob;
			knob_values.add(ezknob.children[1].value)});
		^knob_values;
	}


	freeze_dict{
		var dict, path;
		dict = Dictionary.with(*[
			\name -> this.synthdef,
			\is_bypassed -> this.bypass_button.value,
			\knob_values -> this.freeze_knobs(),
		]);
		^dict = dict;
	}

}


