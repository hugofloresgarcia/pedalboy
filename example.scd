// Look at the pedal directory
PedalBoy.directory

//look at the node tree
s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));

//IMPORTANT: put the entire pedalboy folder inside this path
//run this line to look at the path
Platform.userExtensionDir

// load a pedalboard preset
p = Pedalboard.load()

///MAKING PEDALBOARDS FROM SCRATCH
(
s.waitForBoot({
	//pedalboard object holds a list with pedal pointers
	~pedalboard = Pedalboard.new(
		server: s,
		in_bus: 0,
		out_bus: 0);

	// our pedal objects
	//input buffer
	~input = PedalBoy.input_buffer();
	~pedalboard.add(~input);

	// saw synthesizer
	~saw = PedalBoy.saw_synth();
	~pedalboard.add(~saw);

	//ringmod
	~fshift = PedalBoy.freq_shift();
	~pedalboard.add(~fshift);

	// wah pedal. the ctrl knob is the "expression" pedal
	~wah = PedalBoy.wah();
	~pedalboard.add(~wah);

	//vanilla envelope filter
	~env_filter = PedalBoy.bitcrusher();
	~pedalboard.add(~env_filter);

	//vanilla compressor
	~compressor = PedalBoy.vinyl_boy();
	~pedalboard.add(~compressor);

	//pitch shifter (better than the default ugen)
	~pshift = PedalBoy.g_hex();
	~pedalboard.add(~pshift);


	~delay = PedalBoy.delay();
	~pedalboard.add(~delay);

	~freeverb = PedalBoy.freeverb();
	~pedalboard.add(~freeverb);


	~looper = GrainLooper.looper();
	~pedalboard.add(~looper);
	//assign midi notes to looper buttons
	~looper.assign_loop_controls(
		rec_butt_midinote: 38,
		undo_butt_midinote: 50,
		clear_butt_midinote: 60);

	~panner = PedalBoy.panner();
	~pedalboard.add(~panner);

});
)

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

// if your midi isnt working, check this
MIDIClient.init;
MIDIIn.connectAll;
MIDIFunc.trace;

/// EXTRA: MAKING YOUR OWN PEDALS
//this is how you use the default constructor for your pedals
~myPedal = 	PedalBoy.from_synth_params(
	server: s,
	in: 0,
	out: 0,
	group: nil,
	mappable_arg_dict: Dictionary.with(*[
		//MAPPABLE ARGS ARE YOUR KNOBS
		\gain -> MappableArg.gain(Bus.control(s, 1)),
		//EXAMPLE: A simple volume control
		\volume -> MappableArg.new(
			symbol: \volume,
			bounds: 0@1,           //our limits for the knob,
			default_value: 0.5,    //our default value
			warp: \lin,            //lin or exp warp,
			gui_object: \knob,     //knob is the only one supported at the moment
			bus: Bus.control(s, 1)) //provide it a control bus
	]),
	ugen_func: {
		arg in = 0, out = 0, gain = 1, volume;
		var sig;

		sig = In.ar(in) * gain;

		sig = sig * volume; //make sure you MappableArgs have the same name all around

		ReplaceOut.ar(out, sig);
	},
	name: \mypedal,
	addaction: \addAfter //always use addAfter, except for input/output buffers
);

//add your pedal to the board
~pedalboard.insert(1, ~myPedal);

