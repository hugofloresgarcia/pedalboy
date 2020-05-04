
w = Window.new.front.alwaysOnTop_(true);
~scope_view = View(w, Rect(0, 0, 185, 80));

// set size bounds for VLayout compatibility
~scope_view.minSize_(
	size: Size(
		width: ~scope_view.bounds.width,
		height: ~scope_view.bounds.height
));
~scope_view.maxSize_(
	size: Size(
		width: ~scope_view.bounds.width,
	    height: ~scope_view.bounds.height
));


~scope_bus = Bus.audio(s, 1)


PedalScope(
	server: s,
	numChannels: 1,
	index: ~scope_bus.index, // bus we're scoping  index
	bufsize: 1024,
	zoom: 1,
	rate: 'audio',
	view: ~scope_view) // parent view
.index_(~scope_bus.index) // bus we're scoping index (again)
.view.children[0] // the scope view
.style_(0) // normal style
.fill_(true) // fill below the wave?
.yZoom_(8) // zoom in Y axis
.waveColors_([Color.new(0.5.rrand(0.85),0.5.rrand(0.85), 0.5.rrand(0.85))]) // colorfor the wave
.focus
.start; // start scoping