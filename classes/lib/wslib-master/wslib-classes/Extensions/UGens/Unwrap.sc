// wslib 2010

Unwrap {
	
	// pseudo kr-ugen for retreiving wrapped signals
	
	/* example:
	(
	{	var sig, wrapped;
		sig = SinOsc.kr( Line.kr(5,20,1), 0, 0.5);
		wrapped = Wrap.kr( sig, -0.1, 0.1 );
		[ sig, wrapped, Unwrap.kr( wrapped, -0.1, 0.1 ) ];
	}.plot( 1 );
	)
	
	(
	{	var sig, wrapped;
		sig = SinOsc.ar( 1000, 0, 0.1 ) + SinOsc.ar( 200, 0, 0.5);
		wrapped = Wrap.ar( sig, -0.1, 0.1 );
		[ sig, wrapped, Unwrap.ar( wrapped, -0.1, 0.1 ) ];
	}.plot;
	)
	*/
	
	*ar { arg in = 0.0, lo = 0.0, hi = 1.0; 
		^Integrator.ar( ( HPZ1.ar( in ) * -2 ).round( hi - lo ) ) + in;
	}
	
	*kr { arg in = 0.0, lo = 0.0, hi = 1.0; 
		^Integrator.kr( ( HPZ1.kr( in ) * -2 ).round( hi - lo ) ) + in;
	}
		
}

+ UGen {
	
	/*
	{ LFNoise2.kr( 10 ).wrap2(0.1).unwrap2(0.1) }.plot( 1 );
	
	(
	{ var saw;
		saw = LFSaw.kr( Line.kr(20,-20,1), 0, 0.1 );
		[saw, saw.unwrap2(0.1)];
	}.plot( 1 );
	)
	
	(
	{ var pt;
	  pt = Point( *LFNoise2.kr( 1000.dup ) );
	  [ pt.theta, pt.theta.unwrap2(pi) ] 
	}.plot2(0.1);
	)
	*/
	
	unwrap { |lo = -1, hi = 1|
		^case { rate === 'control' }
			{ Unwrap.kr( this, lo, hi ) }
			{ rate === 'audio' }
			{ Unwrap.ar( this, lo, hi ) }
			{ this };
		}
	
	unwrap2 { |aNumber|
		^this.unwrap( aNumber.neg, aNumber );
		}
}