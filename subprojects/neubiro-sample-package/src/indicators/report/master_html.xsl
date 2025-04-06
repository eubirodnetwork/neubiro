<!--
	~ Copyright 2014-2025 Stefano Gualdi, Fabrizio Carinci, EUBIROD Network.
	~
	~ Licensed under the European Union Public Licence (EUPL), Version 1.1 (the "License");
	~ you may not use this file except in compliance with the License.
	~ You may obtain a copy of the License at
	~
	~       http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
	~
	~ Unless required by applicable law or agreed to in writing, software
	~ distributed under the License is distributed on an "AS IS" BASIS,
	~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	~ See the License for the specific language governing permissions and
	~ limitations under the License.
	-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                exclude-result-prefixes="d">

  <!--
  Docbook stylesheet customisation

  Normally the following are available:
  <xsl:import href="/usr/share/sgml/docbook/xsl-ns-stylesheets/html/chunk.xsl"/>
  <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/chunk.xsl"/>
  <xsl:import href="chunk.xsl"/>

  However, since we use docbook4j we need to use the following instruction for Docbook5

  -->

  <xsl:import href="res:xsl/docbook/xhtml/chunk.xsl" />

  <!-- Basic page setup -->
  <xsl:param name="paper.type" select="'A4'"/>
  <xsl:param name="page.margin.top">0.8cm</xsl:param>
  <xsl:param name="page.margin.bottom">0.8cm</xsl:param>
  <xsl:param name="page.margin.inner">1.5cm</xsl:param>
  <xsl:param name="page.margin.outer">1.5cm</xsl:param>
  <xsl:param name="body.margin.top">0.5in</xsl:param>
  <xsl:param name="body.margin.bottom">0.2in</xsl:param>
  <xsl:param name="body.start.indent">0pt</xsl:param>
  <xsl:param name="body.end.indent">0pt</xsl:param>
  <xsl:param name="region.before.extent">2.5cm</xsl:param>
  <xsl:param name="region.after.extent">2.5cm</xsl:param>

  <xsl:template match="*[@role='red']" priority="10">
    <p style="color:red;font-size:11;font-weight:bold">
     <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="*[@role='normal']" priority="10">
    <h1 style="font-size:14;font-weight:bold;line-height:12pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:template match="*[@role='midsize']" priority="10">
    <h1 style="font-size:11;font-weight:normal;line-height:11pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:template match="*[@role='bmidsize']" priority="10">
    <h1 style="font-size:18;font-weight:bold;line-height:11pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:template match="*[@role='bsmall']" priority="10">
    <h1 style="font-size:14;font-weight:bold;line-height:11pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:template match="*[@role='bverysmall']" priority="10">
    <h1 style="font-size:12;font-weight:bold;line-height:11pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:template match="*[@role='verysmall']" priority="10">
    <h1 style="font-size:12;font-weight:normal;line-height:11pt;line-stacking-strategy:font-height">
     <xsl:apply-templates/>
    </h1>
  </xsl:template>

  <xsl:param name="toc.section.depth">2</xsl:param>

  <!-- Customize TOCs -->
  <xsl:param name="generate.toc">
    appendix toc,title
    article/appendix nop
    article toc,title
    book toc,title
    chapter title
    part toc,title
    preface title
    sect1 title
    sect2 title
    qandadiv toc
    qandaset toc
    reference toc,title
    set toc,title
  </xsl:param>

  <xsl:param name="root.filename">report</xsl:param>
  <xsl:param name="chapter.autolabel" select="'0'"/>
  <xsl:param name="chapter.label.includes.component.label" select="'0'"/>
  <xsl:param name="section.autolabel" select="'0'"/>
  <xsl:param name="section.label.includes.component.label" select="'0'"/>
  <xsl:param name="section.autolabel.max.depth" select="'2'"/>
  <xsl:param name="use.id.as.filename" select="1"/>

  <xsl:param name="table.borders.with.css" select="1"></xsl:param>
  <xsl:param name="table.cell.border.style">solid</xsl:param>
  <xsl:param name="base.dir"></xsl:param>
  <xsl:param name="navig.graphics" select="1"></xsl:param>
  <xsl:param name="navig.graphics.path">../resources/</xsl:param>
  <xsl:param name="chunk.quietly">1</xsl:param>

  <xsl:param name="img.src.path"></xsl:param>
  <xsl:param name="ignore.image.scaling" select="0"></xsl:param>

</xsl:stylesheet>
