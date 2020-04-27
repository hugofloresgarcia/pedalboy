

// Look at the pedal directory
PedalBoy.directory

s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));
//HEADPHONES RECOMMENDED!!!!!
ServerOptionsGui(s)



(
s.waitForBoot({
	MIDIClient.init;
	MIDIIn.connectAll;
	// main window
	w = Window.new(
		name: "PEDALBOY",
		bounds:1040@305).front.alwaysOnTop_(true)
	.onClose_({
		~pedalboard.free_all;
	});

	//pedalboard object holds a list with pedal pointers
	~pedalboard = Pedalboard.new(
		server: s,
		window: w);

	// our pedal objects
	~input = PedalBoy.input_buffer();
	~pedalboard.add(~input);

	~pshift = PedalBoy.pitch_shift();
	~pedalboard.add(~pshift);


	~compressor = PedalBoy.compressor();
	~pedalboard.add(~compressor);

	~vib = PedalBoy.vibrato();
	~pedalboard.add(~vib);


	~delay = PedalBoy.delay();
	~pedalboard.add(~delay);

	~freeverb = PedalBoy.freeverb();
	~pedalboard.add(~freeverb);


	~panner = PedalBoy.panner();
	~pedalboard.add(~panner);

	~pedalboard.assign_bypass(60);
});
)

~pedalboard.remove(1)
~pedalboard.insert(1, Modulator.pulse(~pshift, \interval))

~pedalboard.insert(0, Modulator.noise(~pshift, \interval))

~pedalboard.insert(6, Modulator.tri(~delay, \delay));
~pedalboard.at(0).synth_node.get(\interval, {arg v; v.postln})

~pedalboard.insert(0, Modulator.pulse(~input, \gain));
