/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

/**
 * @author jhostetler
 *
 */
public class PlanetId
{
	public static final int Main_Min = 0;
	public static final int Main_Max = 1;
	public static final int Natural_2nd_Min = 2;
	public static final int Natural_2nd_Max = 3;
	public static final int Natural_3rd_Min = 4;
	public static final int Natural_3rd_Max = 5;
	public static final int Wing_Min = 6;
	public static final int Wing_Max = 7;
	public static final int Center = 8;
	public static final int N = 9;
	
	public static final int Main( final Player player )
	{ return Main_Min + player.ordinal(); }
	
	public static final int Natural_2nd( final Player player )
	{ return Natural_2nd_Min + player.ordinal(); }
	
	public static final int Natural_3rd( final Player player )
	{ return Natural_3rd_Min + player.ordinal(); }
	
	public static final int Wing( final Player player )
	{ return Wing_Min + player.ordinal(); }
}
