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
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://docbook.org/ns/docbook">

  <!--
  Docbook stylesheet customisation

  Normally the following are available:
  <xsl:import href="/usr/share/sgml/docbook/xsl-ns-stylesheets/fo/docbook.xsl"/>
  <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl"/>
  <xsl:import href="docbook.xsl"/>

  However, since we use docbook4j we need to use the following instruction for Docbook5
  (https://blog.javaforge.net/post/37107285148/render-docbook-with-docbook4j)

  -->

  <xsl:import href="res:xsl/docbook/fo/docbook.xsl" />

  <xsl:param name="fop1.extensions" select="1"/>
  <xsl:param name="ignore.image.scaling" select="1"></xsl:param>
  <xsl:template name="book.titlepage.verso"/>

  <xsl:param name="marker.section.level">3</xsl:param>

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

  <xsl:variable name="label">
    <xsl:apply-templates select="." mode="label.markup"/>
  </xsl:variable>

  <xsl:template match="para[@align]">
   <fo:block text-align="{@align}">
    <xsl:apply-templates/>
   </fo:block>
  </xsl:template>

  <!-- Customize chapters and tables titles -->
  <xsl:param name="local.l10n.xml" select="document('')"/>
  <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
    <l:l10n language="en">
      <l:context name="title-numbered">
        <l:template name="chapter" text="%t"/>
      </l:context>
      <l:context name="title">
        <l:template name="table" text="%t"/>
      </l:context>
    </l:l10n>
    <l:l10n language="it">
      <l:context name="title-numbered">
        <l:template name="chapter" text="%t"/>
      </l:context>
      <l:context name="title">
        <l:template name="table" text="%t"/>
      </l:context>
    </l:l10n>
  </l:i18n>

<!-- Not used to keep customised report
  <xsl:param name="chapter.autolabel" select="'1'"/>
  <xsl:param name="chapter.label.includes.component.label" select="'1'"/>
  <xsl:param name="section.autolabel" select="'1'"/>
  <xsl:param name="section.label.includes.component.label" select="'1'"/>
  <xsl:param name="section.autolabel.max.depth" select="'2'"/>
-->

  <xsl:param name="chapter.autolabel" select="'0'"/>
  <xsl:param name="appendix.autolabel" select="'0'"/>

  <xsl:attribute-set name="section.title.level1.properties">
   <xsl:attribute name="border-top">0.5pt solid black</xsl:attribute>
   <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
   <xsl:attribute name="padding-top">6pt</xsl:attribute>
   <xsl:attribute name="padding-bottom">3pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- Font size for Chapter Title -->
  <xsl:attribute-set name="component.title.properties">
    <xsl:attribute name="font-size">16pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- Format Section Title -->
  <xsl:attribute-set name="section.title.level1.properties">
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-size">16pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- Font size for internal titles (eg. tables' titles) -->
  <xsl:attribute-set name="formal.title.properties" use-attribute-sets="normal.para.spacing">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- Font size for internal titles (eg. tables' titles) -->
  <xsl:attribute-set name="chapter.level1.properties" use-attribute-sets="normal.para.spacing">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-size">24pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- Formatting Fonts -->
  <xsl:template match="*[@role='red']" priority="10">
   <fo:block>
    <fo:inline color="red" font-size="12" font-weight="bold">
      <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='normal']" priority="10">
   <fo:block>
	<fo:inline font-size="12" font-weight="normal" line-height="14pt" line-stacking-strategy="font-height">
     <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='midsize']" priority="10">
   <fo:block>
    <fo:inline font-size="8" font-weight="normal" line-height="8pt" line-stacking-strategy="font-height">
      <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='small']" priority="10">
   <fo:block>
    <fo:inline font-size="10" font-weight="normal" line-height="10pt" font-style="normal" text-align="left">
      <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='bmidsize']" priority="10">
   <fo:block>
    <fo:inline font-size="12" font-weight="bold" font-style="normal" text-align="left" line-height="12pt" line-stacking-strategy="font-height">
      <xsl:apply-templates/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='bsmall']" priority="10">
   <fo:block>
    <fo:inline font-size="10" font-weight="bold" font-style="normal" text-align="left" line-height="10pt" line-stacking-strategy="font-height">
      <xsl:apply-templates/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='bverysmall']" priority="10">
   <fo:block>
    <fo:inline font-size="8" color="green" font-weight="bold" font-style="normal" text-align="justify" line-height="8pt" line-stacking-strategy="font-height">
      <xsl:apply-templates/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='verysmall']" priority="10">
   <fo:block>
    <fo:inline font-size="9" font-weight="normal" line-height="10pt" font-style="normal" text-align="left">
      <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:template match="*[@role='extrasmall']" priority="10">
   <fo:block>
    <fo:inline font-size="7" font-weight="normal" line-height="10pt" font-style="normal" text-align="left">
      <xsl:apply-imports/>
    </fo:inline>
   </fo:block>
  </xsl:template>

  <xsl:param name="toc.section.depth">2</xsl:param>

  <!-- Customize TOCs -->
  <xsl:param name="generate.toc">
    appendix title
    article/appendix nop
    article toc,title
    book toc,title
    chapter title
    part toc,title
    preface title
    sect1 title
    sect2 title
    sect3 title
    qandadiv toc
    qandaset toc
    reference toc,title
    set toc,title
  </xsl:param>

  <xsl:attribute-set name="toc.line.properties">
    <xsl:attribute name="font-size">10pt</xsl:attribute>
    <xsl:attribute name="color">blue</xsl:attribute>
    <xsl:attribute name="font-weight">
     <xsl:choose>
      <xsl:when test="fo:appendix">bold</xsl:when>
      <xsl:otherwise>normal</xsl:otherwise>
     </xsl:choose>
    </xsl:attribute>
  </xsl:attribute-set>

  <xsl:template name="table.of.contents.titlepage.recto">
   <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
	         xsl:use-attribute-sets="table.of.contents.titlepage.recto.style"
         	 text-align="center"
	         space-before.minimum="1em"
	         space-before.optimum="1.5em"
	         space-before.maximum="2em"
	         space-after="0.5em"
	         start-indent="0pt"
	         font-size="17.28pt"
	         font-weight="bold"
	         margin-left="{$title.margin.left}"
	         font-family="{$title.fontset}">
    <xsl:choose>
     <xsl:when test="self::book">Contents</xsl:when>
     <xsl:otherwise>
      <xsl:call-template name="gentext">
	  <xsl:with-param name="key" select="'TableofContents'"/>
	  </xsl:call-template>
     </xsl:otherwise>
    </xsl:choose>
   </fo:block>
  </xsl:template>

  <xsl:attribute-set name="toc.margin.properties">
    <xsl:attribute name="break-after">page</xsl:attribute>
  </xsl:attribute-set>

  <!-- Page break -->
  <xsl:template match="processing-instruction('custom-pagebreak')">
    <fo:block break-before='page'/>
  </xsl:template>

  <!-- Line break -->
  <xsl:template match="processing-instruction('custom-linebreak')">
    <fo:block/>
  </xsl:template>

  <!-- Customize tables -->

  <xsl:attribute-set name="table.properties" use-attribute-sets="formal.object.properties">
    <xsl:attribute name="keep-together.within-column">auto</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="table.cell.padding">
    <xsl:attribute name="padding-left">1pt</xsl:attribute>
    <xsl:attribute name="padding-right">1pt</xsl:attribute>
    <xsl:attribute name="padding-top">0.2pt</xsl:attribute>
    <xsl:attribute name="padding-bottom">0.2pt</xsl:attribute>
  </xsl:attribute-set>
  <xsl:param name="keep.row.together">1</xsl:param>

  <!-- http://www.thexmltoolkit.org/codesamples.php?sub=tables -->

  <xsl:template match="table">

   <xsl:variable name="tborder">
    <xsl:choose>
     <xsl:when test="@frame='all'">1</xsl:when>
     <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
   </xsl:variable>

   <xsl:for-each select="tgroup">

    <xsl:variable name="cpadd"><xsl:value-of select="@colsep"/> </xsl:variable>
    <xsl:variable name="cspace"><xsl:value-of select="@rowsep"/></xsl:variable>
    <xsl:variable name="talign"><xsl:value-of select="@align"/></xsl:variable>
    <xsl:variable name="twidth"><xsl:value-of select="@role"/></xsl:variable>

    <table border="{$tborder}" width="{$twidth}" cellpadding="{$cpadd}" cellspacing="{$cspace}" style="align:{$talign};">

     <xsl:for-each select="tbody/row">
      <tr>
       <xsl:for-each select="entry">

        <xsl:variable name="ali"><xsl:value-of select="@align"/></xsl:variable>
        <xsl:variable name="vali"><xsl:value-of select="@valign"/></xsl:variable>
        <xsl:variable name="bgcolor"><xsl:value-of select="@role"/></xsl:variable>

        <xsl:choose>

         <xsl:when test="@spanname">

          <xsl:choose>

           <xsl:when test="@morerows">
            <xsl:variable name="mrows"><xsl:value-of select="@morerows + 1"/></xsl:variable>
            <xsl:variable name="span"><xsl:value-of select="@spanname"/></xsl:variable>
            <xsl:for-each select="ancestor::table/tgroup/spanspec[@spanname = $span]">
             <xsl:variable name="var"><xsl:value-of select="substring-after(@nameend,'c') + (-substring-after(@namest,'c'))+1"/></xsl:variable>

             <xsl:choose>
              <xsl:when test="$bgcolor=''">
               <td rowspan="{$mrows}" colspan="{$var}" style="align:{$ali}; valign:{$vali};">
                <xsl:apply-templates select="ancestor::table/tgroup/tbody/row/entry[@spanname=$span]/para"/>
               </td>
              </xsl:when>
              <xsl:otherwise>
               <td rowspan="{$mrows}" colspan="{$var}" style="align:{$ali}; valign:{$vali}; background-color:{$bgcolor};">
                <xsl:apply-templates select="ancestor::table/tgroup/tbody/row/entry[@spanname=$span]/para"/>
               </td>
              </xsl:otherwise>
             </xsl:choose>

            </xsl:for-each>
           </xsl:when>

           <xsl:otherwise>
            <xsl:variable name="span"><xsl:value-of select="@spanname"/></xsl:variable>
            <xsl:for-each select="ancestor::table/tgroup/spanspec[@spanname = $span]">
             <xsl:variable name="var">
              <xsl:value-of select="substring-after(@nameend,'c') + (-substring-after(@namest,'c'))+1"/>
             </xsl:variable>
             <xsl:choose>
              <xsl:when test="$bgcolor=''">
               <td colspan="{$var}" style="align:{$ali}; valign:{$vali};">
                <xsl:apply-templates select="ancestor::table/tgroup/tbody/row/entry[@spanname=$span]/para"/>
               </td>
              </xsl:when>
              <xsl:otherwise>
               <td colspan="{$var}" style="align:{$ali}; valign:{$vali}; background-color:{$bgcolor};"><xsl:apply-templates select="ancestor::table/tgroup/tbody/row/entry[@spanname=$span]/para"/></td>
              </xsl:otherwise>
             </xsl:choose>
            </xsl:for-each>
           </xsl:otherwise>

          </xsl:choose>

         </xsl:when>

         <xsl:when test="@morerows">
          <xsl:variable name="mrows"><xsl:value-of select="@morerows + 1"/></xsl:variable>
          <xsl:variable name="col"><xsl:value-of select="@colname"/></xsl:variable>
          <xsl:variable name="cwidth">
           <xsl:value-of select="ancestor::table/tgroup/colspec[@colname=$col]/@colwidth"/>
          </xsl:variable>
          <xsl:choose>
           <xsl:when test="$bgcolor=''">
            <td rowspan="{$mrows}"  style="width:{$cwidth}; align:{$ali}; valign:{$vali};"><xsl:apply-templates select="para"/>&#160;</td>
           </xsl:when>
           <xsl:otherwise>
            <td rowspan="{$mrows}"  style="width:{$cwidth}; align:{$ali}; valign:{$vali}; background-color:{$bgcolor};"><xsl:apply-templates select="para"/>&#160;</td>
           </xsl:otherwise>
          </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
          <xsl:variable name="col"><xsl:value-of select="@colname"/></xsl:variable>
          <xsl:variable name="cwidth">
           <xsl:value-of select="ancestor::table/tgroup/colspec[@colname=$col]/@colwidth"/>
          </xsl:variable>
          <xsl:choose>
           <xsl:when test="$bgcolor=''">
            <td style="width:{$cwidth}; align:{$ali}; valign:{$vali};"><xsl:apply-templates select="para"/>&#160;</td>
           </xsl:when>
           <xsl:otherwise>
            <td style="width:{$cwidth}; align:{$ali}; valign:{$vali}; background-color:{$bgcolor};"><xsl:apply-templates select="para"/>&#160;</td>
           </xsl:otherwise>
          </xsl:choose>
         </xsl:otherwise>
        </xsl:choose>
       </xsl:for-each>
      </tr>
     </xsl:for-each>

    </table>

   </xsl:for-each>

  </xsl:template>

  <!-- This template avoids creating a new table row within the table for each entry cell para -->
  <xsl:template match="entry/para">
   <xsl:if test="@role='skip'"><br/><br/></xsl:if>
   <xsl:if test="@role='noskip'"><br/></xsl:if>
   <span class="ctcoltxt12"><xsl:apply-templates/></span>
  </xsl:template>

  <xsl:param name="header.column.widths">2 0 2</xsl:param>
  <xsl:param name="footer.column.widths">2 0 2</xsl:param>

  <xsl:template name="header.content">
    <xsl:param name="pageclass" select="''"/>
    <xsl:param name="position" select="''"/>
    <xsl:param name="gentext-key" select="''"/>

    <fo:block>
      <xsl:choose>

        <xsl:when test="$position = 'left'">
         <xsl:call-template name="draft.text"/>
          <fo:block>
           <fo:block font-weight="bold">
            <xsl:apply-templates select="." mode="object.title.markup"/>
           </fo:block>
          </fo:block>
          <fo:block>
	       <fo:retrieve-marker retrieve-class-name="chapter.head.marker"
                               retrieve-position="first-including-carryover"
                               retrieve-boundary="page-sequence"/>
          </fo:block>
        </xsl:when>

        <xsl:when test="$position = 'center'">
         <fo:external-graphic src="url(resources/eubirod-network-logo.png)" width="1.1in" height="auto"
                              content-width="scale-to-fit" content-height="scale-to-fit"
                              content-type="content-type:image/png" text-align="center"/>
         <xsl:call-template name="draft.text"/>
        </xsl:when>

        <xsl:when test="$position = 'right'">
          <fo:retrieve-marker retrieve-class-name="section.head.marker"
                              retrieve-position="first-including-carryover"
                              retrieve-boundary="page-sequence"/>
        </xsl:when>

      </xsl:choose>
    </fo:block>

  </xsl:template>

  <xsl:template name="footer.content">
    <xsl:param name="pageclass" select="''"/>
    <xsl:param name="position" select="''"/>
    <xsl:param name="gentext-key" select="''"/>

    <fo:block>
      <xsl:choose>

        <xsl:when test="$position = 'left'">
          <fo:block color="red">
	       <fo:external-graphic src="url(resources/neubiro-logo-slim.png)" width="0.5in" height="auto"
                              content-width="scale-to-fit" content-height="scale-to-fit"
                              content-type="content-type:image/png" text-align="center"/>

          <xsl:call-template name="draft.text"/>
          Powered</fo:block>
        </xsl:when>

        <xsl:when test="$position = 'center'">
         <xsl:call-template name="draft.text"/>
        </xsl:when>

        <xsl:when test="$position = 'right'">
         <fo:block font-size="14"><fo:page-number/></fo:block>
        </xsl:when>

      </xsl:choose>
    </fo:block>

  </xsl:template>

</xsl:stylesheet>
