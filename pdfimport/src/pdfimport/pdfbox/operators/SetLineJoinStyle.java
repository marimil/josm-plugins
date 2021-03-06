/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pdfimport.pdfbox.operators;

import java.awt.BasicStroke;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.util.PDFOperator;

import pdfimport.pdfbox.PageDrawer;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:andreas@lehmi.de>Andreas Lehmkühler</a>
 * @version $Revision: 1.0 $
 */
public class SetLineJoinStyle extends org.apache.pdfbox.util.operator.SetLineJoinStyle
{

	/**
	 * Set the line cap style.
	 * @param operator The operator that is being executed.
	 * @param arguments List
	 *
	 * @throws IOException If an error occurs while processing the font.
	 */
	@Override
	public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
	{
		super.process( operator, arguments );
		int lineJoinStyle = context.getGraphicsState().getLineJoin();
		PageDrawer drawer = (PageDrawer)context;
		BasicStroke stroke = drawer.getStroke();
		if (stroke == null)
		{
			drawer.setStroke( new BasicStroke(1,BasicStroke.CAP_SQUARE,lineJoinStyle) );
		}
		else
		{
			drawer.setStroke( new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), lineJoinStyle,
					stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()) );
		}
	}
}
