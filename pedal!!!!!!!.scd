

// Look at the pedal directory
Pedal.directory

s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));
//HEADPHONES RECOMMENDED!!!!!
ServerOptionsGui(s)



(
s.waitForBoot({
	MIDIClient.init;
	MIDIIn.connectAll;
	// main window
	w = Window.new(
		name: "PEDALBOARD",
		bounds:1040@305).front.alwaysOnTop_(true)
	.onClose_({
		~pedalboard.free_all;
	});

	//pedalboard object holds a list with pedal pointers
	~pedalboard = Pedalboard.new(s, w);

	// our pedal objects
	~input = Pedal.input_buffer();
	~pedalboard.add(~input);

	~pshift = Pedal.pitch_shift();
	~pedalboard.add(~pshift);


	~compressor = Pedal.compressor();
	~pedalboard.add(~compressor);

	~vib = Pedal.vibrato();
	~pedalboard.add(~vib);


	~delay = Pedal.delay();
	~pedalboard.add(~delay);

	~freeverb = Pedal.freeverb();
	~pedalboard.add(~freeverb);


	~panner = Pedal.panner();
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
