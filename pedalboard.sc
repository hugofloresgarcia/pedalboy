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

	init{|server, window|
		this.server = server;

		this.pedal_bounds = Rect(0, 0, 200, 200);
		this.window = window;

		this.window.addFlowLayout;
		this.group = Group.new(this.server, \addToHead);
		this.pedals = List.new();

		if(this.window.isNil.not, {
/*			this.view = View(
				parent: this.window,
				bounds: this.window.bounds)
			.addFlowLayout;*/
		});
	}

	init_pedal{|pedal, target|
		pedal.init(this.server, this.patch_cable, this.patch_cable, target);

		if(this.window.isNil.not, {
			pedal.make_view(this.window.view, this.pedal_bounds);
		});

		pedal.on;
	}

	at{|index|
		^this.pedals.at(index).dereference;
	}

	add{|pedal|
		var target;
		if(this.pedals.size == 0, {
			target = this.group
		}, {
			target = this.at(this.pedals.size-1).node;
		});
		target = this.group;
		this.init_pedal(pedal, target);
		this.pedals.add(Ref(pedal));
	}

	insert{|index, pedal|
		var target;
		 // since the pedals add to to the tail of a target,
		// we get the pedal that goes before our new pedal,
		// and add our new pedal to the tail of it.
		target = this.at(index-1).node;
		this.init_pedal(pedal, target);
		this.pedals.insert(index, pedal);
	}
}

 