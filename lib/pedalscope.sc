PedalScope : Stethoscope{
	var <>scopeView;

	initStethoscope{ arg server_, parent, bus_, bufsize_, cycle_;
		var singleBus;

		server = server_;


		synth = BusScopeSynth(server);

		maxBufSize = max(bufsize_, 128);

		bus = bus_;
		singleBus = bus.class === Bus;

		aBusSpec = ControlSpec(0, server.options.numAudioBusChannels, step:1);
		cBusSpec = ControlSpec(0, server.options.numControlBusChannels, step:1);
		if( singleBus ) {
			busSpec = if(bus.rate===\audio){aBusSpec}{cBusSpec};
		};

		cycleSpec = ControlSpec( maxBufSize, 64, \exponential );
		yZoomSpec = ControlSpec( 0.125, 16, \exponential );
		cycle = cycleSpec.constrain(cycle_);
		yZoom = 1.0;

		smallSize = Size(180,70);
		largeSize = Size(500,500);

		makeGui = { arg parent;
			var gizmo;

			// WINDOW, WRAPPER VIEW

			if( window.notNil ) {window.close};

			if( parent.isNil ) {
				view = window = Window(
					bounds: (smallSize).asRect.center_(Window.availableBounds.center)
				).name_("Stethoscope");
			}{
				view = View( parent, parent.bounds );
				window = nil;
			};

			// WIDGETS

			scopeView = ScopeView()
			.background_(Color.new255(51, 51, 51))
			.style_(0)
			.fill_(true)
			.yZoom_(5)
			.waveColors_([Color.rand(0.75, 0.8)]);
			scopeView.server = server;
			scopeView.canFocus = true;

			cycleSlider = Slider().orientation_(\horizontal).value_(cycleSpec.unmap(cycle));
			yZoomSlider = Slider().orientation_(\vertical).value_(yZoomSpec.unmap(yZoom));

			rateMenu = PopUpMenu().items_(["Audio","Control"]).enabled_(singleBus);
			idxNumBox = NumberBox().decimals_(0).step_(1).scroll_step_(1).enabled_(singleBus);
			chNumBox = NumberBox().decimals_(0).step_(1).scroll_step_(1)
			.clipLo_(1).clipHi_(128).enabled_(singleBus);

			if( singleBus ) {
				rateMenu.value_(if(bus.rate===\audio){0}{1});
				idxNumBox.clipLo_(busSpec.minval).clipHi_(busSpec.maxval).value_(bus.index);
				chNumBox.value_(bus.numChannels);
			};

			styleMenu = PopUpMenu().items_(["Tracks","Overlay","X/Y"]);

			// LAYOUT

			gizmo = "999".bounds( idxNumBox.font ).width + 20;
			idxNumBox.fixedWidth = gizmo;
			chNumBox.fixedWidth = gizmo;
			idxNumBox.align = \center;
			chNumBox.align = \center;

			view.layout =
			GridLayout()
			.add(
/*				HLayout(
					rateMenu,
					idxNumBox,
					chNumBox,
					nil,
					styleMenu
				).margins_(0).spacing_(2), 0, 0*/
			)
			.add(scopeView,1,0)
/*			.add(yZoomSlider.maxWidth_(15), 1,1)
			.add(cycleSlider.maxHeight_(15), 2,0)*/
			.margins_(2).spacing_(2);

			// ACTIONS

			cycleSlider.action = { |me| setCycle.value(cycleSpec.map(me.value)) };
			yZoomSlider.action = { |me| setYZoom.value(yZoomSpec.map(me.value)) };
			idxNumBox.action = { |me| setIndex.value(me.value) };
			chNumBox.action = { |me| setNumChannels.value(me.value) };
			rateMenu.action = { |me| setRate.value(me.value) };
			styleMenu.action = { |me| setStyle.value(me.value) };
			view.asView.keyDownAction = { |v, char, mod| this.keyDown(char, mod) };
			view.onClose = { view = nil; this.quit; };

			// LAUNCH

			scopeView.focus;
			if( window.notNil ) { window.front };
		};


		setCycle = { arg val;
			cycle = val;
			synth.setCycle(val);
		};

		setYZoom = { arg val;
			yZoom = val;
			scopeView.yZoom = val;
		};

		// NOTE: assuming a single Bus
		setIndex = { arg i;
			bus = Bus(bus.rate, i, bus.numChannels, bus.server);
			synth.setBusIndex(i);
		};

		// NOTE: assuming a single Bus
		setNumChannels = { arg n;
			// we have to restart the whole thing:
			bus = Bus(bus.rate, bus.index, n, bus.server);
			updateColors.value;
			this.run;
		};

		// NOTE: assuming a single Bus
		setRate = { arg val;
			val.switch (
				0, {
					bus = Bus(\audio, bus.index, bus.numChannels, bus.server);
					busSpec = aBusSpec;
				},
				1, {
					bus = Bus(\control, bus.index, bus.numChannels, bus.server);
					busSpec = cBusSpec;
				}
			);
			synth.setRate(val);
			idxNumBox.clipLo_(busSpec.minval).clipHi_(busSpec.maxval).value_(bus.index);
			this.index = bus.index; // ensure conformance with busSpec;
			updateColors.value;
		};

		setStyle = { arg val;
			if(this.numChannels < 2 and: { val == 2 }) {
				"Stethoscope: x/y scoping with one channel only; y will be a constant 0".warn;
			};
			scopeView.style = val;
		};

		updateColors = {
			var colors;
			bus.do { |b|
				var c = if(b.rate === \audio){Color.new255(255, 218, 000)}{Color.new255(125, 255, 205)};
				colors = colors ++ Array.fill(b.numChannels, c);
			};
			scopeView.waveColors = colors;
		};

		makeGui.value(parent);
		updateColors.value;

		ServerTree.add(this, server);
		ServerQuit.add(this, server);
		this.run;
	}
}