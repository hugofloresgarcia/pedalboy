Pedalboard{
	var
	<>server,
	<>in_bus,
	<>out_bus,
	<>patch_cable,
	<>pedals,
	<>group,

	<>view,
	<>pedal_bounds,
	<>controls,
	<>crazy_aa,
	<>window;

	*new{|server, in_bus, out_bus, window = nil|
		PedalBoy.make_dir;
		server = server ? Server.default;
		if(server.hasBooted.not, {
			Error("server has not been booted").throw;
		});
		MIDIClient.init;
		MIDIIn.connectAll;
		MIDIClient.init;
		MIDIIn.connectAll;
		^super.new.init(server, in_bus, out_bus, window);
	}

	init{|server, in_bus, out_bus, window|
		"initing pedalboard..".postln;
		this.server = server;

		this.pedal_bounds = Rect.fromPoints(0@0, 180@(270));
		if (window.isNil, {
			window = Window.new(
				name: "PEDALBOY",
				bounds:((this.pedal_bounds.width+5)*4)@(this.pedal_bounds.height+5 + 110),
				scroll: true
			)
			.background_(Color.new255(51, 51, 51))
			.front
			.alwaysOnTop_(true)
			.acceptsMouseOver_(true);
			window.view.onResize_({
				arg v;
				// if(v.bounds.width > ((this.pedal_bounds.width+5)*6),{
					this.remake_view();
				// });
			});
		});
		this.window = window;
		this.window.onClose_({
			arg w;
			"freeing all synths".postln;
			this.free_all;
		});

		this.in_bus = in_bus;
		this.out_bus = out_bus;

		this.patch_cable = Bus.audio(this.server, 1);

		this.window.addFlowLayout;
		this.group = Group.new(this.server, \addToHead);
		this.pedals = List.new();

		this.window.background_(Color.rand(0.55, 0.75));

		this.crazy_aa = Task({
			inf.do({
				this.reset_colors();
				0.1.wait;
			});
		});

		if(this.window.isNil.not, {
		});
	}

	init_pedal{|pedal, target|
		var in, out;
		if(pedal.synthdef == \input_buffer, {
			"input buffer connected!".postln;
			in = this.in_bus;
		},{
			in = this.patch_cable;
		});
		if(pedal.synthdef == \panner, {
			"panner connected!".postln;
			out = this.out_bus;
		},{
			out = this.patch_cable;
		});

		pedal.init(this.server, in, out, target);
		pedal.parent = this;
		pedal.on;

		((pedal.synthdef == \input_buffer) || (pedal.synthdef == \panner)).not.if({
			"bypassing ".post; pedal.synthdef.postln;
			pedal.bypass;
		});
	}

	set_in_bus{|bus|
		var input_pedal = this.at(0);
		this.in_bus = bus;
		input_pedal.out = bus;
		input_pedal.arg_dict[\in] = bus;
		input_pedal.is_bypassed.not.if({
			input_pedal.synth_node.set(\in, bus);
		});
		"set input bus to ".post; bus.postln;

	}
	set_out_bus{|bus|
		var output_pedal = this.at(this.pedals.size-1);
		this.out_bus = bus;
		output_pedal.out = bus;
		output_pedal.arg_dict[\out] = bus;
		output_pedal.is_bypassed.not.if({
			output_pedal.synth_node.set(\out, bus);
		});
		"set output bus to ".post; bus.postln;
	}

	make_label_menu{|index|
		var pedal = this.at(index);
		var label_view = pedal.view;
		var pedalboy_dir;
		var insert_before_menu;
		var insert_after_menu;
		var modulate_menu;
		//change color when we're over them
		label_view.children[0].mouseOverAction_({
			arg v;
			v.background_(
				v.background.blend(Color.white, 0.8);
			);
		});

		insert_before_menu = Menu().title_("insert before...");
		insert_after_menu = Menu().title_("insert after...");
		modulate_menu = Menu().title_("modulate...");

		PedalBoy.directory.keysValuesDo({
			|key, value|
			insert_before_menu.addAction(MenuAction(key, {this.insert(index, value.value)}));
			insert_after_menu.addAction(MenuAction(key, {this.insert(index+1, value.value)}));
		});


		pedal.mappable_args.keys.do({
			arg m_arg;
			var menu;
			menu = Menu(
				MenuAction("sine",        {this.insert(index, Modulator.sine       (pedal, m_arg))}),
				MenuAction("tri",         {this.insert(index, Modulator.tri        (pedal, m_arg))}),
				MenuAction("pulse",       {this.insert(index, Modulator.pulse      (pedal, m_arg))}),
				MenuAction("noise",       {this.insert(index, Modulator.noise      (pedal, m_arg))}),
				MenuAction("brown_noise", {this.insert(index, Modulator.brown_noise(pedal, m_arg))})
			).title_(m_arg);
			modulate_menu.addAction(menu);
		});

		label_view.setContextMenuActions(
			insert_before_menu,
			insert_after_menu,
			modulate_menu,
			MenuAction("assign bypass", {
				EZNumber.new(
					parent: nil,
					bounds: 160@50,
					label: "bypass (midinote)",
					controlSpec: ControlSpec(0, 127, \lin),
					action: {arg v; pedal.assign_bypass(v.value.asInteger) ; v.view.parent.close},
					initVal: 0,
					initAction: false)
				.alwaysOnTop_(true);
			}),
			MenuAction("remove", {this.remove(index)}),
		);
/*
		//drag to move pedals
		pedal.view.beginDragAction_({
			arg v, x, y;
			// v.moveTo(x, y);
			index;
		});
		pedal.view.dragLabel_(pedal.synthdef);
		pedal.view.canReceiveDragHandler_({
			arg v, x, y;
			v.background_(pedal.focused_color);
		});
		pedal.view.receiveDragHandler_({
			arg v, x, y;
			var moveFromIndex = View.currentDragString.interpret;
			defaultGetDrag

			moveFromIndex.postln;
			index.postln;
			"moving".postln;
			this.move_to(moveFromIndex, index);
			DragBoth
			"here?".postln;
		});
/*		pedal.view.mouseMoveAction_({
			arg v, x, y;
			v.beginDrag(x, y);
		});*/
		pedal.view.mouseLeaveAction_({
			arg v, x, y;
			// v.background_(pedal.normal_color);
		});
		pedal.view.mouseUpAction_({
			arg v, x, y;
			// v.background_(pedal.normal_color);
		});*/
	}

	assign_bypass{|start_note|
		this.pedals.do({
			arg pedal_ptr, count;
			var pedal;

			pedal = pedal_ptr.dereference;
			pedal.assign_bypass(start_note + count);
		});
	}

	at{|index|
		^this.pedals.at(index).dereference;
	}

	add{|pedal|
		var target;
		if (pedal.isMemberOf(Modulator), {
			"couldn't add a modulator, please insert before your target".warn;
		}, {
			if(this.pedals.size == 0, {
				target = this.group;
			}, {
				target = this.at(this.pedals.size-1).node;
			});
			this.init_pedal(pedal, target);
			this.pedals.add(Ref(pedal));
			this.remake_view();
		});

	}


	insert{|index, pedal|
		var target;

		if (pedal.isMemberOf(Modulator), {
			pedal.on;
		});
		if (pedal.isMemberOf(PedalBoy), {
			// since the pedals add to to the tail of a target,
			// we get the pedal that goes before our new pedal,
			// and add our new pedal to the tail of it
			target = this.at(index-1).node;
			this.init_pedal(pedal, target);
		});

		this.pedals.insert(index, `pedal);
		this.remake_view();
	}

	disconnect{|index|
		//removes pedal from chain but only pauses the synth
		var pedal = this.at(index);
		pedal.bypass;
		this.pedals.removeAt(index);
		this.remake_view();
	}

	remove{|index|
		//removes pedal from chain and frees the synth
		var pedal = this.at(index);
		pedal.free;
		this.pedals.removeAt(index);
		this.remake_view;
	}

	move_to{|index_from, index_to|
		//move pedal from an index to another, preserving all of its info
		var pedal = this.at(index_from);
		var target = this.at(index_to - 1).node;
		//free the synth
		pedal.free;
		//reset target
		// pedal.group = target;
		"pedal freed".postln;
		//disconnect the pedal
		this.disconnect(index_from);
		"disconnect".postln;
		//insert back into  our signal
		this.insert(index_to, pedal);
		"inserted".postln;
	}


	clear{
		this.pedals.size.do({
			arg index;
			this.remove(0);
		});
	}

	resize_if_necessary{
		var num_pedals = this.pedals.size;
		var width = this.window.view.bounds.width;
		var height = this.window.view.bounds.height;
		var p_width = this.pedal_bounds.width;
		var p_height = this.pedal_bounds.height;

		var necessary = ((num_pedals+4) * p_width * p_height) > (width*height);

		if(necessary, {
			if((num_pedals-1).mod(4) == 0 && (this.pedals.size != 0), {
				height = height + p_height + 5;
			});
			this.window.view.resizeTo(width, height);
		});


	}

	remake_view{
		this.resize_if_necessary;
		this.window.view.removeAll;

		this.window.layout_(nil);
		this.window.addFlowLayout();
		this.make_control_view();
		this.pedals.do({
			arg pedal_ptr, count;
			var pedal = pedal_ptr.dereference;

			pedal.make_view(this.window, this.pedal_bounds);
			this.make_label_menu(count);
		});
		this.window.view.decorator.nextLine;

		// this.server.makeGui(this.window);

		this.window.onClose_({
			arg w;
			"freeing all synths".postln;
			this.free_all;
		});


		this.window.view.children.do({
			arg v;
			v.background_(Color.rand(0.75, 0.95))
		});
		this.window.refresh;
	}

	make_control_view{
		this.controls = View(this.window, this.window.bounds.width@30);
		this.controls.addFlowLayout();

		Button(this.controls, 50@20)
		.minSize_(Size(50, 20))
		.maxSize_(Size(50, 20))
		.states_([
			["LOAD", Color.new255(51, 51, 51), Color.rand(0.75, 0.95)]
		])
		.action_({
			this.load()
		})
		.background_(Color.rand(0.75, 0.95));

		Button(this.controls(44@20))
		.minSize_(Size(44, 20))
		.maxSize_(Size(44, 20))
		.states_([
			["SAVE", Color.new255(51, 51, 51), Color.rand(0.75, 0.95)]
		])
		.action_({
			this.save()
		})
		.background_(Color.rand(0.75, 0.95));

		EZNumber(
			parent: this.controls,
			bounds: 130@20,
			label: "input bus",
			controlSpec: ControlSpec(0, 64, step: 1),
			action: {arg v; this.set_in_bus(v.value.asInteger)},
			initVal: 0,
			initAction: false)
		.setColors(
					stringBackground: Color.rand(0.75, 0.95),
					stringColor: Color.rand(0.05, 0.15),
					numBackground: Color.rand(0.75, 0.95),
					numStringColor: Color.rand(0.05, 0.15),
					numNormalColor: Color.rand(0.05, 0.15)
				);

		EZNumber(
			parent: this.controls,
			bounds: 130@20,
			label: "output bus",
			controlSpec: ControlSpec(0, 64, step: 1),
			action: {arg v; this.set_out_bus(v.value.asInteger)},
			initVal: 0,
			labelWidth: 80,

			initAction: false)
		.setColors(
					stringBackground: Color.rand(0.75, 0.95),
					stringColor: Color.rand(0.05, 0.15),
					numBackground: Color.rand(0.75, 0.95),
					numStringColor: Color.rand(0.05, 0.15),
					numNormalColor: Color.rand(0.05, 0.15)
				);
	}



	save_compile_str{
		var compile_str;
		var pedal_list = List.new();

		this.pedals.do({
			arg pedal_ptr, count;
			var pedal;
			pedal = pedal_ptr.dereference;
			pedal_list.add(pedal.freeze_dict);
		});

		^compile_str = pedal_list.asCompileString;
	}

	restore_from_compile_str{|compile_str|
		var dict_list;

		this.clear;

		dict_list = compile_str.interpret;
		dict_list.do({
			arg pedal_dict;
			var pedal;

			if(PedalBoy.directory.at(pedal_dict[\name]).isNil.not, {
				if(pedal_dict[\name] == \mod, {},{
					pedal = PedalBoy.directory.at(pedal_dict[\name]).value;
					this.add(pedal)
				});
			});

		});
		this.pedals.do({
			arg pedal_ptr, count;
			var pedal = pedal_ptr.dereference;
			pedal.set_all_knobs(dict_list[count][\knob_values]);
		});
		this.pedals.do({
			arg pedal_ptr, count;
			var pedal = pedal_ptr.dereference;
			pedal.bypass_button.valueAction_(dict_list[count][\is_bypassed]);
		});
	}

	save{
		Dialog.savePanel(
			okFunc: {
				arg path;
				var f;
				var str = this.save_compile_str();
				var add;

				if(path.endsWith(".pdlbrd"),{
					add = ""}, {add= ".pdlbrd"});
				f = File.new((path ++ add), "w");
				f.write(str);
				f.close;
				// Archive.write(path);
		}, path: thisProcess.platform.userExtensionDir)

	}
	/*	save{|unique_name|
	Archive.global.put(unique_name, this);
	}*/

	*load{|server, in_bus, out_bus, window = nil|
		var instance;

		instance = Pedalboard(
			server: server,
			in_bus: in_bus,
			out_bus: out_bus,
			window: window);

		instance.load;
		/*Dialog.openPanel(
		okFunc: {
		arg path;
		instance = Archive.read(path);
		}, path: thisProcess.platform.userExtensionDir);
		*/
		// instance = Archive.global.at(unique_name, this);
		^instance;
	}

	load{
		var compile_str;
		var dict_list;

		Dialog.openPanel(
			okFunc: {
				arg path;
				compile_str = File.new(path, "r").readAllString;
				this.restore_from_compile_str(compile_str);
		}, path: thisProcess.platform.userExtensionDir);
	}

	reset_colors{
		this.window.view.children.do({
			arg pedalview;
			pedalview.background_(Color.rand(0.75, 0.95));
			pedalview.children.do({
				arg sub_view;
				// sub_view.background_(Color.rand(0.75, 0.95));
				sub_view.children.do({
					arg subsubview;
					subsubview.background_(Color.rand(0.75, 0.95))
				});
			})
		})
	}


	go_crazy_aaaa_go_stupid{|bool = true|
		if(bool, {
			this.crazy_aa.reset;
			this.crazy_aa.play(AppClock);
		}, {
			this.crazy_aa.stop;
		});
	}





	free_all{
		this.pedals.do({
			arg pedal_ptr;
			var pedal;

			pedal = pedal_ptr.dereference;

			pedal.free;
			pedal.post; " freed".postln;
		});
	}


}

