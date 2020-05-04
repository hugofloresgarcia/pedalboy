MappableArg {
	var
	<>symbol,
	<>bounds,
	<>control_spec,
	<default_value,
	<>gui_object,
	<>bus,
	<>warp;


	*new{|symbol, bounds, default_value, warp, gui_object, bus|
		^super.new.init(symbol, bounds, default_value, warp, gui_object, bus);
	}


	*gain{|bus|
		^MappableArg(
			symbol: \gain,
			bounds: 1e-2@1,
			default_value: 1,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}

	*wet{|bus|
		^MappableArg.new(
			symbol: \wet,
			bounds: 0.01@1,
			default_value: 0.8,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}


	*dry{|bus|
		^MappableArg.new(
			symbol: \dry,
			bounds: 0.01@1,
			default_value: 1,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}

	*cutoff{|bus|
		^MappableArg.new(
			symbol: \cutoff,
			bounds: 100@20e3,
			default_value: 3e3,
			warp: \exp,
			gui_object: \knob,
			bus: bus);
	}

	*atk{|bus|
		^MappableArg.new(
			symbol: \atk,
			bounds: 0.001@5,
			default_value: 0.1,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}

	*rel{|bus|
		^MappableArg.new(
			symbol: \rel,
			bounds: 0.001@5,
			default_value: 0.1,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}

	*pos{|bus|
		^MappableArg.new(
			symbol: \pos,
			bounds: -1@1.0,
			default_value: 0,
			warp: \lin,
			gui_object: \knob,
			bus: bus);
	}



	init{|symbol, bounds, default_value, warp, gui_object, bus|
		this.symbol = symbol;
		this.bounds = bounds;

		this.warp = warp;

		this.gui_object = gui_object;
		this.bus = bus;

		this.bus.setSynchronous(default_value);
		this.default_value = default_value;
		this.control_spec = ControlSpec(this.bounds.x, this.bounds.y, this.warp);
	}

	as_synth_arg{
		^this.bus.asMap;
	}


	default_value_{|val|
		this.bus.setSynchronous(val);
	}

}