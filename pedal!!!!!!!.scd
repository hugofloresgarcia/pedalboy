

// Look at the pedal directory
Pedal.directory


s.boot;

(
s.waitForBoot({
w = Window.new(bounds:500@500).front.alwaysOnTop_(true);
~pedalboard = Pedalboard.new(s, w);

~input = Pedal.input_buffer();
~saw = Pedal.saw_synth();
~sine = Pedal.sine_synth();
~panner = Pedal.panner();


~pedalboard.add(~input);

~pedalboard.add(~saw);

~pedalboard.add(~sine);

~pedalboard.add(~panner);
});
)

~sine.node.isRunning
~sine.node.isPlaying

s.plotTreeView(0.5, Window.new.front.alwaysOnTop_(true));

w = Window.new.front.alwaysOnTop_(true);
p = Pedal.saw_synth(s, 0, 0, nil)
p.make_view(w, Rect(0, 0, 200, 200))

w.view.minSizeHint





//these work now!
~pedalboard.toggle_off(2);
~pedalboard.toggle_off(1);
~pedalboard.toggle_off(0);

~pedalboard.toggle_on(0);
~pedalboard.toggle_on(2);
~pedalboard.toggle_on(1);






~saw.scope(\interval)
~saw.node.get(\wet, {arg v; v.postln})
~saw.node.set(\dry, 0)

~pedalboard.at(1).node.get(\wet, {arg v; v.postln})


Synth

s.scope;
s.freqscope;
s.plotTree.alwaysOnTop_(true)


( //here the third view is fit to both the right and bottom innerBounds
w = Window.new;
w.view.decorator = d = FlowLayout.new( w.view.bounds, 10@10, 20@5 );
Slider2D( w.view,140@140 ).background_( Color.rand );
Slider2D( w.view,140@140 ).background_( Color.rand );
d.nextLine;
Slider2D( w.view, d.indentedRemaining ).background_( Color.rand );
w.front;
)



//ideas for pedals:


//make a lofi compressor pedal using Dust, Compander, and Pitchshifter, RLPF in honor of the zvex lofi junky
//something with Klank?
//gverb//freeverb
//combc delays
//envelope filter
//


Pedal.directory


Pedalboard








