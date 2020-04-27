Pedalboard{
	var
	<>server,
	<>patch_cable,
	<>pedals,
	<>group,

	<>view,
	<>pedal_bounds,
	<>window;

	*new{|server, window = nil|
		^super.new.init(server, window);
	}

	init{|server, window, pedal_bounds|
		this.server = server;

		this.pedal_bounds = Rect.fromPoints(0@0, 200@(300));
		this.window = window;

		this.patch_cable = Bus.audio(this.server, 1);

		this.window.addFlowLayout;
		this.group = Group.new(this.server, \addToHead);
		this.pedals = List.new();

		if(this.window.isNil.not, {
		});
	}

	init_pedal{|pedal, target|
		var in, out;
		if(pedal.synthdef == \input_buffer, {
			"input buffer connected!".postln;
			in = 0
		},{
			in = this.patch_cable;
		});
		if(pedal.synthdef == \panner, {
			"panner connected!".postln;
			out = 0
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
		if (pedal.isMemberOf(Pedal), {
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

 