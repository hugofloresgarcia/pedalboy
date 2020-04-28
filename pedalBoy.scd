// Look at the pedal directory
PedalBoy.directory

//look at the node tree
s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));

//IMPORTANT: put the entire pedalboy folder inside this path
//run this line to look at the path
Platform.userExtensionDir

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

	// a cool fm synth. monophonic
	~fm = PedalBoy.fm_synth();
	~pedalboard.add(~fm);

	// wah pedal. the ctrl knob is the "expression" pedal
	~wah = PedalBoy.wah();
	~pedalboard.add(~wah);

	//vanilla envelope filter
	~env_filter = PedalBoy.env_filter();
	~pedalboard.add(~env_filter);

	//vanilla compressor
	~compressor = PedalBoy.compressor();
	~pedalboard.add(~compressor);

	//pitch shifter (better than the default ugen)
	~pshift = PedalBoy.pitch_shift();
	~pedalboard.add(~pshift);

	//vibrato
	~vib = PedalBoy.vibrato();
	~pedalboard.add(~vib);

	~delay = PedalBoy.delay();
	~pedalboard.add(~delay);

	~freeverb = PedalBoy.freeverb();
	~pedalboard.add(~freeverb);

	~panner = PedalBoy.panner();
	~pedalboard.add(~panner);

	//assign bypass buttons to midinotes, ascending chromatically in order
	~pedalboard.assign_bypass(60);
});
)

// save current pedalboard configuration
~pedalboard.save

//load a previous pedalboard configuration. make sure to initialize server
s.boot;
~pedalboard = Pedalboard.load(s, 0, 0, nil)

//insert a pedal into our signal chain
~pedalboard.insert(1, PedalBoy.wah());

//access a pedal instance in our signal chain
~wah = ~pedalboard.at(1);

//modulate ANY knob in our gui using a Modulator
~mod = Modulator.sine(
	parent: ~wah,
	argument: \ctrl);
~pedalboard.insert(1, ~mod);

//remove a pedal from our pedalboard
Pedalboard.remove(1);

//easter egg: party mode
~pedalboard.go_crazy_aaaa_go_stupid(true)

//assign a knob to a ccNum
~pshift.assign_knob(1, \ctrl);





(
SynthDef(\freeze, {
	arg in = 0, out = 0, wet = 0.5, dry = 0.7, rate = 1, trigger;
	var sig, grainMaker, buftrigger, trigRate, centerPos, overlap = 4, dur = 0.01, bufnum;

	bufnum = LocalBuf.new(48e3 * 0.1);

	in = SoundIn.ar(in);

	RecordBuf.ar(
		inputArray: in,
		bufnum: bufnum,
		offset: 0.0,
		recLevel: 1,
		preLevel: 0,
		run: trigger-0.5,
		loop: 0,
		trigger: trigger-0.5,
		doneAction: 0);

/*	trigRate = overlap / dur;
	centerPos = BufDur.kr(bufnum) * 0.25;
	buftrigger = Impulse.ar(trigRate);

	centerPos = Phasor.ar(
		trig: 0,
		rate:  1/BufSampleRate.kr(bufnum) / 1,
		start: 0,
		end: BufFrames.kr(bufnum)
	);


	sig = TGrains.ar(
		numChannels: 2,
		trigger: buftrigger,
		bufnum: bufnum,
		rate: rate,
		centerPos: centerPos,
		dur: dur,
		// pan: LFNoise1.kr(1),
		amp: 0.3,
		interp: 4);*/



	sig = PlayBuf.ar(
		numChannels: 1,
		bufnum: bufnum,
		rate: BufRateScale.kr(bufnum) * rate,
		trigger: trigger-0.5,
		startPos: 0.0,
		loop: 1,
		doneAction: 0);


	sig = Mix.ar([sig * wet, in * dry]);
	// Out.ar(out, sig);
}).add;
)

a =Synth(\freeze, [\trigger, 1]).set(\dry, 0)
a.set(\trigger, 0)

Routine({
~freeze = {
	arg feedback = 1, sustain = 1;
	var sig, local;

	sig = SoundIn.ar(0);
	local = LocalIn.ar(2) + sig.dup * sustain;

	15.do({
			local = AllpassN.ar(local, 0.06, Rand(0.001, 0.06), inf)
		});

	LocalOut.ar(local);

	Out.ar(0, sig + local);
}.play;
0.06.wait;
	// ~freeze.set(\sustain, 0)
}).play

~freeze.set(\feedback, 0);

















