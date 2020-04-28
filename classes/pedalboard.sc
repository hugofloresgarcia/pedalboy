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
	<>crazy_aa,
	<>window;

	*new{|server, in_bus, out_bus, window = nil|
		server = server ? Server.default;
		if(server.hasBooted.not, {
			Error("server has not been booted").throw;
		});
		MIDIClient.init;
		MIDIIn.connectAll;
		^super.new.init(server, in_bus, out_bus, window);
	}

	init{|server, in_bus, out_bus, window|
		this.server = server;

		this.pedal_bounds = Rect.fromPoints(0@0, 200@(300));
		if (window.isNil, {
			window = Window.new(
				name: "PEDALBOY",
				bounds:1040@305).front.alwaysOnTop_(true)
			.onClose_({
				arg w;
				this.free_all;
			});
		});
		this.window = window;

		this.in_bus = in_bus;
		this.out_bus = out_bus;

		this.patch_cable = Bus.audio(this.server, 1);

		this.window.addFlowLayout;
		this.group = Group.new(this.server, \addToHead);
		this.pedals = List.new();

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
		pedal.on;

		if(this.window.isNil.not, {
			pedal.make_view(this.window.view, this.pedal_bounds);
		});
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
		if(this.pedals.size == 0, {
			target = this.group;
		}, {
			target = this.at(this.pedals.size-1).node;
		});
		this.init_pedal(pedal, target);
		this.pedals.add(Ref(pedal));
		this.remake_view();
	}


	insert{|index, pedal|
		var target;

		if (pedal.isMemberOf(Modulator), {
			pedal.on;
			this.pedals.insert(index, pedal);
			this.remake_view();
		});
		if (pedal.isMemberOf(PedalBoy), {
			// since the pedals add to to the tail of a target,
			// we get the pedal that goes before our new pedal,
			// and add our new pedal to the tail of it.

			target = this.at(index-1).node;


			this.init_pedal(pedal, target);
			this.pedals.insert(index, pedal);

			this.remake_view();
		});
	}

	remove{|index|
		var pedal = this.at(index);
		pedal.free;
		this.pedals.removeAt(index);
		this.window.view.children[index].removeAll;
		this.remake_view;
	}

	clear{
		this.pedals.size.do({
			arg index;
			this.remove(0);
		});
	}


	remake_view{
		if(((this.pedals.size).mod(5) == 0) && (this.pedals.size != 0), {
			this.window.view.resizeTo(
				width: this.window.bounds.width,
				height: this.window.bounds.height + this.pedal_bounds.height)
		});
		this.window.view.removeAll;

		this.window.layout_(nil);
		this.window.addFlowLayout();
		this.pedals.do({
			arg pedal_ptr, count;
			var pedal = pedal_ptr.dereference;

			pedal.make_view(this.window, this.pedal_bounds);
		});
		this.window.refresh;
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
			pedal = PedalBoy.directory.at(pedal_dict[\name]);
			this.add(pedal)
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
		}, path: thisProcess.platform.userExtensionDir)

	}

	*load{|server, in_bus, out_bus, window = nil|
		var instance;

		instance = Pedalboard(
			server: server,
			in_bus: in_bus,
			out_bus: out_bus,
			window: window);

		instance.load;

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


	toggle_on{|index|
		this.at(index).on;
	}

	toggle_off{|index|
		this.at(index).bypass;
	}

	free_all{
		this.pedals.do({
			arg pedal_ptr;
			var pedal;

			pedal = pedal_ptr.dereference;

			pedal.free
		});
	}


}

