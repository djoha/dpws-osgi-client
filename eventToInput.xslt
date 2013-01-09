<xsl:transform version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:param name="newAddress"/>
   <xsl:param name="newAction"/>

	<xsl:template match="/">
		<s12:Envelope xmlns:dpws="http://schemas.xmlsoap.org/ws/2006/02/devprof"
			xmlns:s12="http://www.w3.org/2003/05/soap-envelope" xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">
			<s12:Header>
				<wsa:Action><xsl:value-of select="$newAction"/></wsa:Action>
				<wsa:MessageID>urn:uuid:6648d070-3885-11e2-8028-d7edce9ed125</wsa:MessageID>
				<wsa:To><xsl:value-of select="$newAddress"/></wsa:To>
			</s12:Header>
			<s12:Body>
    			<xsl:copy-of select="/*[local-name() = 'Envelope']/*[local-name() ='Body']/*"/>
			</s12:Body>
		</s12:Envelope>
	</xsl:template>
</xsl:transform>