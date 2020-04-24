

// Look at the pedal directory
Pedal.directory

(
s.waitForBoot({
	w = Window.new(bounds:500@500).front.alwaysOnTop_(true);

	~pedalboard = Pedalboard(s, w);

	~pedalboard.add(Pedal.input_buffer());
	~pedalboard.add(Pedal.sine_synth());
	~pedalboard.add(Pedal.panner());

});
)



//ideas for pedals:
//make a lofi compressor pedal using Dust, Compander, and Pitchshifter, RLPF in honor of the zvex lofi junky
//something with Klank?
//gverb//freeverb
//combc delays
//envelope filter
//
