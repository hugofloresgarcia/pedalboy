/*
wslib 2013

a "smooth" version of MultiSlider

*/

SmoothMultiSlider : SmoothSlider {
	
	var currentIndex;
	var <selected;
	
	draw {
		var startAngle, arcAngle, size, widthDiv2, aw;
		var knobPosition, n, realKnobSize;
		var rect, drawBounds, radius;
		var baseRect, knobWidth;
		var center, strOri;
		
		var bnds; // used with string
		
		Pen.use {
			
			rect = this.drawBounds;
				
			drawBounds = rect.insetBy( border, border );
			
			if( orientation == \h )
				{  drawBounds = Rect( drawBounds.top, drawBounds.left, 
					drawBounds.height, drawBounds.width );
					
				   // baseRect = drawBounds.insetBy( (1-baseWidth) * (drawBounds.width/2), 0 );
				   
				   Pen.rotate( 0.5pi, (rect.left + rect.right) / 2, 
				   					 rect.left + (rect.width / 2)  );
				};
			
			baseRect = drawBounds.insetBy( (1-baseWidth) * (drawBounds.width/2), 0 );
			
			size = drawBounds.width;
			widthDiv2 = drawBounds.width * 0.5;
					
			realKnobSize = (knobSize * drawBounds.width / value.size.max(1))
					.max( thumbSize ).min( drawBounds.height );
			knobPosition = drawBounds.top + ( realKnobSize / 2 )
						+ ( (drawBounds.height - realKnobSize) * (1- value).max(0).min(1))
				.asCollection;
			n = knobPosition.size;
			radius = (knobSize * drawBounds.width) / 2 / knobPosition.size;
			knobWidth = baseRect.width / knobPosition.size;
			
			Pen.use{	
			color[0] !? { // base / background
				//Pen.fillColor = color[0];
				Pen.roundedRect( baseRect, radius.min( baseRect.width/2) );
				color[0].penFill( baseRect );
				};
			
			if( backgroundImage.notNil )
				{ 
				Pen.roundedRect( baseRect, radius.min( baseRect.width/2) );
				backgroundImage[0].penFill( baseRect, *backgroundImage[1..] );
				}
			};
			
			Pen.use{
			color[2] !? { // // border
				if( border > 0 )
					{ 
					
					  if( color[2].notNil && { color[2] != Color.clear } )
					  	{	 Pen.strokeColor = color[2];
						  Pen.width = border;
						  Pen.roundedRect( baseRect.insetBy( border/(-2), border/(-2) ), 
						  	radius.min( baseRect.width/2) + (border/2) ).stroke;
						  };
					  if( extrude )
					  	{ 
					  	Pen.use{	
						  	  Pen.rotate( (h: -0.5pi, v: 0 )[ orientation ],
					   				(rect.left + rect.right) / 2, 
					   				rect.left  + (rect.width / 2)  );
					   		
						  	  Pen.extrudedRect( 
						  	  	baseRect.rotate((h: 0.5pi, v: 0 )[ orientation ],
					   				(rect.left + rect.right) / 2, 
					   				rect.left  + (rect.width / 2))
					   					.insetBy( border.neg, border.neg ), 
						  		(if( radius == 0 ) 
						  			{ radius } { radius + border }).min( baseRect.width/2 ),
						  		border, 
						  		inverse: true )
						  	}
					  	};
					};
				};
				};
				
			this.drawFocusRing( 
				baseRect.insetBy( border.neg , border.neg ), 
				radius.min( baseRect.width/2) + border 
			);
			
			
			Pen.use{		
				
				if( selected.size > 0 ) {
					selected.do({ |i|
						var radiusMul;
						radiusMul = [ 
							if( i == 0 ) { 1 } { 0 }, // is first
							if( (i+1) == n ) { 1 } { 0 }, // is last
						].mirror2;
						Pen.roundedRect( Rect.fromPoints( 
							(baseRect.left + (knobWidth * i))@baseRect.top,
							(baseRect.left + (knobWidth * (i+1)))@baseRect.bottom ), 
						radius.min( baseRect.width/2) * radiusMul );
					});
					
					Color.black.alpha_(0.1).penFill;
				};
				
				color[1] !? { 
					//color[1].set; // hilight
					if( isCentered ) {	
						knobPosition.do({ |knobPosition, i|
							var centerPosition, radiusMul;
							centerPosition = baseRect.bottom.blend( baseRect.top, centerPos ); 
							radiusMul = [ 
								1, 1,
								if( (i+1) == n ) { 1 } { 0 }, // is last
								if( i == 0 ) { 1 } { 0 }, // is first
							];
							
							if( knobPosition > centerPosition ) { radiusMul = radiusMul.reverse };
							
							Pen.roundedRect( Rect.fromPoints( 
									(baseRect.left + (knobWidth * i))@
										((knobPosition - (realKnobSize / 2))
											.min( centerPosition ) ),
									(baseRect.left + (knobWidth * (i+1)))@
										((knobPosition + (realKnobSize / 2))
											.max( centerPosition ) ))
									
								, radius * radiusMul ); //.fill;
							
						});
						color[1].penFill( baseRect );
					} {	
						knobPosition.do({ |knobPosition, i|
							var radiusMul;
							radiusMul = [ 
								1, 1,
								if( (i+1) == n ) { 1 } { 0 }, // is last
								if( i == 0 ) { 1 } { 0 }, // is first
							];
							Pen.roundedRect( Rect.fromPoints( 
									(baseRect.left + (knobWidth * i))@(knobPosition - (realKnobSize / 2)),
									(baseRect.left + (knobWidth * (i+1)))@baseRect.bottom ), 
									radius.min( baseRect.width/2) * radiusMul );
						});
						
						color[1].penFill( baseRect );
					};
				};
					
			};
			
			
				
			Pen.use{
	
			color[3] !? {	 
				knobPosition.do({ |knobPosition, i|
					var knobRect;
					
					knobRect =  Rect.fromPoints(
						Point( drawBounds.left + (knobWidth * i), 
							( knobPosition - (realKnobSize / 2) ) ),
						Point( drawBounds.left + (knobWidth * (i+1)), 
							knobPosition + (realKnobSize / 2) ) );
	
					Pen.roundedRect( knobRect, radius );//.fill; 
					
					color[3].penFill( knobRect ); // requires extGradient-fill.sc methods
					
					 if( extrude && ( knobRect.height >= border ) )
						  	{ 
						  	Pen.use{	
							  	  Pen.rotate( (h: -0.5pi, v: 0 )[ orientation ],
						   				(rect.left + rect.right) / 2, 
						   				rect.left  + (rect.width / 2)  );
						   		
							  	  Pen.extrudedRect( 
							  	  	knobRect.rotate((h: 0.5pi, v: 0 )[ orientation ],
						   				(rect.left + rect.right) / 2, 
						   				rect.left  + (rect.width / 2)), 
							  		radius.max( border ), border * knobBorderScale)
							  	}
						  	};
					});
				}
				
			};
			
			if( enabled.not )
				{
				Pen.use {
					Pen.fillColor = Color.white.alpha_(0.5);
					Pen.roundedRect( 
						baseRect.insetBy( border.neg, border.neg ), 
						radius.min( baseRect.width/2) ).fill;
					};
				};
			
			};
	}
	
	sliderBounds {
		var realKnobSize, drawBounds, rect;
		
		rect = this.drawBounds;
				
		drawBounds = rect.insetBy( border, border );
				
		if( orientation == \h )
				{  drawBounds = Rect( drawBounds.top, drawBounds.left, 
					drawBounds.height, drawBounds.width ); 
				};
				
		realKnobSize = ((knobSize / this.value.asCollection.size) * drawBounds.width)
					.max( thumbSize ).min( drawBounds.height );
		
		
		^drawBounds.insetBy( 0, realKnobSize / 2 );
	}
	
	setLine { |indices, values, active = true|
		var newValue, size;
		newValue = value.copy;
		size = newValue.size;
		values = values.asCollection;
		
		indices.asCollection.doAdjacentPairs({ |a,b,i|
			var start, end;
			if( a != b ) {
				start = values.wrapAt(i) ? 0.5;
				end = values.wrapAt(i+1) ? 0.5;
				(a..b).do({ |item|
					if( item.exclusivelyBetween( -1, size ) ) {
						newValue[item] = item.linlin(a,b,start,end,\none);
					};
				});
			} {
				newValue[b] = values.wrapAt(i+1);
			};
		});
		
		if( active ) {
			this.valueAction = newValue;
		} {
			this.value = newValue;
		};
	}
	
	getMouseValue { |point, bounds|
		bounds = bounds ?? { this.drawBounds };
		if( orientation === \h ) {
			point = Point(point.y, bounds.right - (point.x)); 
			bounds = Rect( bounds.top, bounds.left, bounds.height, bounds.width );
		};
		if( thumbSize < bounds.height ) {	 
			^1 - ((point.y - (bounds.top + (	
					( (knobSize / value.size) * bounds.width )
						.max( thumbSize.min( bounds.height ) ) / 2)
				)
			) / (bounds.height - ((knobSize / value.size) * bounds.width ).max( thumbSize ))
			)
		} {
			^nil
		};
	}
	
	getMouseIndex { |point, bounds|
		bounds = bounds ?? { this.drawBounds };
		if( orientation === \h ) {
			point = Point(point.y, bounds.right - (point.x)); 
			bounds = Rect( bounds.top, bounds.left, bounds.height, bounds.width );
		};
		^point.x.linlin( bounds.left, bounds.right, 0, value.size ).floor.min( value.size - 1);
	}
	
	selected_ { |values|
		selected = values;
		this.refresh;
	}
	
	mouseDown { arg x, y, modifiers, buttonNumber, clickCount, noAction = false;
		var bounds, oldValue, newValue;
		var index;
		if( enabled ) {		
			if( noAction != true ) {
				mouseDownAction.value( this, x, y, modifiers, buttonNumber, clickCount );
			};
			bounds = this.drawBounds; 
			value = value.asCollection;
			hit = Point(x, y); 
			index = this.getMouseIndex( hit, bounds );
			newValue = this.getMouseValue( hit, bounds ) ? value[ index ];
			
			if( ModKey( modifiers ).shift.not ) {
				if( selected.asCollection.includes( index ) ) {
					hitValue = newValue;
				} {
					oldValue = value.copy;
					value[ index ] = newValue.round( step ? 0 );
					selected = [index];
					deltaAction.value( this, value - oldValue );
					this.clipValue;
	
					if( allwaysPerformAction or: { oldValue != value } ) { 
						action.value(this, x, y, modifiers); 
					};
				};
			} {
				if( selected.asCollection.includes( index ).not ) {
					selected = selected.add( index );
				};
			};
				
			this.refresh;
			currentIndex = index;
		};
		
	}

	mouseMove { arg x, y, modifiers;
		var pt, angle, inc = 0;
		var bounds, oldValue, newValue, delta;
		var index;
		if( enabled ) {	
			mouseMoveAction.value( this, x, y, modifiers );
			bounds = this.drawBounds;
			pt = Point(x, y);
			index = this.getMouseIndex( pt, bounds );			
			if (modifiers != 1048576, { // we are not dragging out - apple key
				if( ModKey( modifiers ).shift.not ) {	
					if( hitValue.isNil ) {	
						newValue = this.getMouseValue( pt, bounds ) ? value[ index ];
						oldValue = value.copy;
						
						if( index != currentIndex ) {
							this.setLine( [ currentIndex, index ], [ value[ currentIndex ], newValue ], false );
						} {
							value[ index ] = newValue.round( step ? 0 );
						};
							
						selected = [ index ];
					} {
						newValue = this.getMouseValue( pt, bounds ) ? hitValue;
						oldValue = value.copy;
						if( newValue != hitValue ) {
							this.selectedValues_( this.selectedValues + (newValue - hitValue), false );
						};
						hitValue = newValue;
					};
					
					deltaAction.value( this, value - oldValue );
					this.clipValue;
						
					if( allwaysPerformAction or: { oldValue != value } ) { 
						action.value(this, x, y, modifiers); 
					};
				} {
					if( index != currentIndex ) {
						(index..currentIndex).do({ |index|
							if( selected.asCollection.includes( index ).not ) {
								selected = selected.add( index );
							};
						});
					};
				};
									
				this.refresh;
				currentIndex = index;
			});
		};
	}
	
	mouseUp { |x, y, modifiers, buttonNumber|
		if( hitValue.isNil ) {
			if( ModKey( modifiers ).shift.not ) {
				 selected = nil;
			};
		} {
			if( hit == Point(x,y) ) {
				selected = nil;
			};
		};
		this.refresh;
		hitValue = nil;
		mouseUpAction.value( this, x, y, modifiers, buttonNumber ); 
	}
	
	size { ^value.size }
	size_ { |n| if( n.notNil ) { this.value = this.value.extend( n, 0 ) }; }
	
	selectedValues {
		if( selected.notNil ) {
			^value[ selected ];
		} {
			^value;
		};
	}
	
	selectedValues_ { |values, active = true|
		if( selected.notNil ) {
			selected.do({ |index, i|
				value[ index ] = values.clipAt(i);
			});
		} {
			value = values;
		};
		if( active ) {
			this.valueAction = value;
		} {
			this.value = value;
		};
	}
	
	increment { |zoom=1| ^this.selectedValues = 
		( this.selectedValues + (max(this.step ? 0, this.pixelStep) * zoom) ).min(1); 
	}
	decrement { |zoom=1| ^this.selectedValues = 
		( this.selectedValues - (max(this.step ? 0, this.pixelStep) * zoom) ).max(0); 
	}
		
	keyDown { arg char, modifiers, unicode, keycode, key;
		var zoom = this.getScale(modifiers); 
		var arrowKey = unicode.getArrowKey ? key.getArrowKey;
		
		// standard keydown
		if (char == $r, { this.selectedValues = {1.0.rand}.dup( this.selectedValues.size ); });
		if (char == $n, { this.selectedValues = 0.0.dup( this.selectedValues.size ); });
		if (char == $x, { this.selectedValues = 1.0.dup( this.selectedValues.size ); });
		if (char == $c, { this.selectedValues = centerPos.dup( this.selectedValues.size ); });
		if (char == $], { this.increment(zoom); ^this });
		if (char == $[, { this.decrement(zoom); ^this });
			
		switch( arrowKey,
			\up, { this.increment(zoom); ^this },
			\right, { this.increment(zoom); ^this },
			\down, { this.decrement(zoom); ^this },
			\left, { this.decrement(zoom); ^this }
		);
		
		^nil;
		
	}
}