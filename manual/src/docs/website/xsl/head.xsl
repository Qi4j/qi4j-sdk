<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template name="user.head.content">
  <xsl:text disable-output-escaping="yes">
<![CDATA[

<!-- favicon -->

<link rel="shortcut icon" href="http://qi4j.org/favicon.ico" type="image/vnd.microsoft.icon" />
<link rel="icon" href="http://qi4j.org/favicon.ico" type="image/x-icon" />

<!-- style -->

<link href="css/shCore.css" rel="stylesheet" type="text/css" />
<link href="css/shCoreEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/shThemeEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/qi4j.css" rel="stylesheet" type="text/css" />

<!-- Syntax Highlighter -->

<script type="text/javascript" src="js/shCore.js"></script>
<script type="text/javascript" src="js/shBrushJava.js"></script>
<script type="text/javascript" src="js/shBrushJScript.js"></script>
<script type="text/javascript" src="js/shBrushBash.js"></script>
<script type="text/javascript" src="js/shBrushPlain.js"></script>
<script type="text/javascript" src="js/shBrushXml.js"></script>
<script type="text/javascript" src="js/shBrushGroovy.js"></script>
<script type="text/javascript" src="js/shBrushPython.js"></script>
<script type="text/javascript" src="js/shBrushRuby.js"></script>

<!-- activate when needed
<script type="text/javascript" src="js/shBrushCSharp.js"></script>
-->
 
<script type="text/javascript">
  SyntaxHighlighter.defaults['tab-size'] = 4;
  SyntaxHighlighter.defaults['gutter'] = false;
  SyntaxHighlighter.defaults['toolbar'] = false;
  SyntaxHighlighter.all()
</script>

<!-- JQuery -->

<script type="text/javascript" src="js/jquery-1.6.4.min.js"></script>

<!-- Image Scaler -->

<script type="text/javascript" src="js/imagescaler.js"></script>

<!-- Table Styler -->

<script type="text/javascript" src="js/tablestyler.js"></script>

<!-- Version Switcher -->

<script type="text/javascript" src="js/versionswitcher.js"></script>
]]>
  </xsl:text>
  </xsl:template>

</xsl:stylesheet>

