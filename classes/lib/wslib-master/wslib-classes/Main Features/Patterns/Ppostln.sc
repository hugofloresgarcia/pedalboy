Ppostln : Pattern {
	var <>pattern, <>prefix;
	*new { |pattern, prefix|
		^super.newCopyArgs(pattern, prefix)
	}
	storeArgs { ^[pattern] }
	embedInStream { arg inval;
		var outval;
		var stream = pattern.asStream;
		loop {
			prefix !? _.post;
			outval = stream.next(inval).postln;
			inval = outval.yield;
		};
	}
}

+ Pattern {
	ppostln { ^Ppostln( this ) }
	poll { |prefix| ^Ppostln( this, prefix ) }
}