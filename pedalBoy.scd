// Look at the pedal directory
PedalBoy.directory

//look at the node tree
s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));

//IMPORTANT: put the entire pedalboy folder inside this path
//run this line to look at the path
Platform.userExtensionDir

~pedalboard.save()
~pedalboard.load
p = Pedalboard.load()

NodeWatcher.newFrom(s).start
NodeWatcher.register(~pedalboard.at(0).node, true)
~pedalboard.at(0).node.isPlaying

//fix bypass at begginning and at insert/remove and remake view
//drag and drop
//feedback
//patch cables
//vocoder with filter banks
//phase vocoder with fft?

Buffer.freeAll


~pedalboard.at(10).looper_buttons.children.at(0)

(
s.waitForBoot({
	//pedalboard object holds a list with pedal pointers
	~pedalboard = Pedalboard.new(
		server: s,
		in_bus: 0,
		out_bus: 0);

	// our pedal objects
	~input = PedalBoy.input_buffer();
	~pedalboard.add(~input);

	~saw = PedalBoy.saw_synth();
	~pedalboard.add(~saw);

	~fshift = PedalBoy.freq_shift();
	~pedalboard.add(~fshift);

	// wah pedal. the ctrl knob is the "expression" pedal
	~wah = PedalBoy.wah();
	~pedalboard.add(~wah);

	~pedalboard.insert(1, PedalBoy.futh());

	//vanilla envelope filter
	~env_filter = PedalBoy.bitcrusher();
	~pedalboard.add(~env_filter);

	//vanilla compressor
	~compressor = PedalBoy.vinyl_boy();
	~pedalboard.add(~compressor);

	//pitch shifter (better than the default ugen)
	~pshift = PedalBoy.g_hex();
	~pedalboard.add(~pshift);

	/*	//vibrato
	~vib = PedalBoy.vibrato();
	~pedalboard.add(~vib);*/

	~delay = PedalBoy.delay();
	~pedalboard.add(~delay);

	~freeverb = PedalBoy.freeverb();
	~pedalboard.add(~freeverb);


	~looper = GrainLooper.looper();
	~pedalboard.add(~looper);
	~looper.assign_loop_controls(38, 50, 60);

	~panner = PedalBoy.panner();
	~pedalboard.add(~panner);

	// assign bypass buttons to midinotes, ascending chromatically in order
	// ~pedalboard.assign_bypass(60);
});
)

~pedalboard.at(10).assign_loop_controls(38, 50, 60)
~pedalboard.at(10).server = s

~looper.scope_bus
~looper.play_synths.at(0).trace

~input.assign_knob(91, \gain);

~wah.assign_knob(91, \ctrl); b
~pshift.assign_knob(91, \interval);

~pedalboard.insert(0, Modulator.brown_noise(~input, \gain));
~pedalboard.insert(4, Modulator.brown_noise(~wah, \ctrl))

~fm.assign_knob(11, \index)
~pedalboard.patch_cable

~pedalboard.insert(1, Modulator.sine(~fm, \index));

~pedalboard.insert(10, Modulator.brown_noise(~panner, \pos));
~pedalboard.insert(10, Modulator.noise(~panner, \pos));

~vib.assign_knob(11, \speed);
~env_filter.assign_knob(91, \min);
~pshift.assign_knob(93, \wet);

MIDIClient.init;
MIDIIn.connectAll;
MIDIFunc.trace;


// save current pedalboard configuration
~pedalboard.save

//load a previous pedalboard configuration. make sure to initialize server
~pedalboard = Pedalboard.load(s, 0, 0, nil)

//insert a pedal into our signal chain
~pedalboard.insert(1, PedalBoy.wah());

//access a pedal instance in our signal chain
~wah = ~pedalboard.at(1);

//modulate ANY knob in our gui using a Modulator
~mod = Modulator.noise(
	parent: ~wah,
	argument: \ctrl);
~pedalboard.insert(2, ~mod);

//remove a pedal from our pedalboard
~pedalboard.remove(1);

//easter egg: party mode
~pedalboard.go_crazy_aaaa_go_stupid(true)

//assign a knob to a ccNum
~pshift.assign_knob(1, \ctrl);

//assign bypass to a noteON
~pshift.assign_bypass(69);
//move pedal from index a to index b
~pedalboard.move_to(3, 1)

/// EXTRA: MAKING YOUR OWN PEDALS

Latch for downsampler


