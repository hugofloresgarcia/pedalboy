

// Look at the pedal directory
Pedal.directory

s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));
//HEADPHONES RECOMMENDED!!!!!

(
s.waitForBoot({
	// main window
	w = Window.new(
		name: "PEDALBOARD",
		bounds:1040@305).front.alwaysOnTop_(true);

	//pedalboard object holds a list with pedal pointers
	~pedalboard = Pedalboard.new(s, w);

	// our pedal objects
	~input = Pedal.input_buffer();

	~pshift = Pedal.pitch_shift();
	~compressor = Pedal.compressor();
	~fm = Pedal.fm_synth();
	~freeverb = Pedal.freeverb();
	~delay = Pedal.delay();
	~panner = Pedal.panner();

	~pedalboard.add(~input);
	~pedalboard.add(~compressor);
	~pedalboard.add(~pshift);
	~pedalboard.add(~fm);
	~pedalboard.add(~delay);
	~pedalboard.add(~freeverb);
	~pedalboard.add(~panner);
});
)


ServerOptionsGui(s)
Spectogram
~pedalboard.insert(0, Modulator.noise(~pshift, \interval))

~pedalboard.insert(6, Modulator.tri(~delay, \delay));
~pedalboard.at(0).synth_node.get(\interval, {arg v; v.postln})

~pedalboard.insert(0, Modulator.pulse(~input, \gain));
