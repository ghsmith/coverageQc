<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html"/>

    <xsl:template match="/vcf">

        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html>
        </xsl:text>
    
        <html>

            <xsl:text disable-output-escaping="yes">
            &lt;!--
                author: geoffrey.hughes.smith@gmail.com
                -->
            </xsl:text>
            
            <head>
                <title>Coverage QC Report</title>
                <script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
                <script type="text/javascript" src="http://code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
                <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css"/>
                <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.13.3/jquery.tablesorter.min.js"></script>
                <link rel="stylesheet" type="text/css" href="http://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.13.3/css/theme.default.css"/>
                <script type="text/javascript" src="http://www.google.com/jsapi"></script>
                <script type="text/javascript">
                    var chartWidth;
                    google.load("visualization", "1", {packages:["corechart"]});
                    google.setOnLoadCallback(function() {
                    $(document).ready(function() {

                    // set up read count histograms
                    $(".readHistogram").each(function() {
                    $(this).css("background-position", "0px " + ((1.0 * (100 - parseInt($(this).attr("data-pct")))) / 100) * $(this).outerHeight() + "px");
                    });

                    // engage the tablesorter
                    $("#qcReportTable").tablesorter({
                    headers: {
                    0: { sorter:false },
                    1: { sorter:false },
                    2: { sorter:false },
                    3: { sorter:false },
                    4: { sorter:"text" },
                    5: { sorter:"text" },
                    6: { sorter:"text" },
                    7: { sorter:"text" },
                    8: { sorter:"text" },
                    9: { sorter:"text" },
                    10: { sorter:"text" },
                    11: { sorter:"text" },
                    }
                    });
                                
                    // expand all takes so long that a modal "wait..." is displayed
                    $("#geneExonExpandCollapseAllButton").bind("click", function() {
                    if($(this).html() == "+") {
                    $(this).html("-");
                    $(".geneExonExpandCollapseButton").html("-");
                    $("#blocker").show();
                    $(".geneExon_child").show();
                    setTimeout(function() {
                    var geneExonPos = 1;
                    $(".geneExon_child").each(function() {
                    eval("geneExon" + geneExonPos + "_drawChart()");
                    geneExonPos++;
                    });
                    $("#blocker").hide();
                    }, 10);
                    }
                    else {
                    $(this).html("+");
                    $(".geneExonExpandCollapseButton").html("+");
                    $(".geneExon_child").hide();
                    }
                    return false;
                    });
                
                    $(".geneExonExpandCollapseButton").bind("click", function() {
                    $("." + $(this).attr('id') + "_child").toggle();
                    if($(this).html() == "+") {
                    $(this).html("-");
                    eval($(this).attr('id') + "_drawChart()");
                    }
                    else {
                    $(this).html("+");
                    }
                    return false;
                
                    });
                
                    $("#exportLink").bind("click", function() {
                    showExportDialog();
                    return false;
                    });
                
                    });
                    });
                </script>
                <style>
                
                    body { 
                    font-family: Arial; 
                    }

                    ul {
                    padding-left: 20px;
                    }

                    table.dataTable {
                    width: auto;
                    font: inherit;
                    border-collapse: collapse;
                    border-spacing: 0px;
                    margin-left: 0px;
                    }

                    table.dataTable > thead > tr > th {
                    color: black;
                    font: inherit;
                    border: 1px solid black !important;
                    padding: 4px;
                    vertical-align: middle; 
                    }

                    table.dataTable > tbody > tr > td {
                    color: black;
                    font: inherit;
                    border: 1px solid black;
                    padding: 4px;
                    vertical-align: middle; 
                    }

                    table.dataTable > tbody > tr > td, table.dataTable > thead > tr > th {
                    page-break-inside: avoid;
                    }

                    table.dataTable > tbody > tr > td.readHistogram {
                    background-color: white;
                    background-image: url(data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAeAB4AAD/4QAiRXhpZgAATU0AKgAAAAgAAQESAAMAAAABAAEAAAAAAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCABkAGQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9DKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP/2Q==);
                    background-repeat:no-repeat;
                    }
                
                    table.footNoteTable td {
                    font-size: x-small;
                    border: 0px;
                    padding: 0px;
                    }
                                
                    #blocker {
                    display: none;
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    opacity: .7;
                    background-color: black;
                    z-index: 1000;
                    text-align: center;
                    vertical-align: middle;
                    }
                
                    #blocker div {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    color: white;
                    font-size: x-large;
                    font-weight: bold;
                    }
                
                </style>
            </head>
        
            <body>

                <div id="blocker">
                    <div>wait...</div>
                </div>
            
                <h1>Coverage QC Report</h1>
                <table>
                    <tr style="vertical-align: top;">
                        <td>version</td>
                        <td>:</td>
                        <td style="font-weight: bold;"> 
                            <xsl:value-of select="/vcf/@version"/> (build <xsl:value-of select="/vcf/@build"/>)</td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>report run date</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:value-of select="substring(/vcf/@runDate, 1, 16)"/>
                        </td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>gVCF file</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:value-of select="/vcf/@fileName"/>
                        </td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>variant TSV file</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:choose>
                                <xsl:when test="@variantTsvFileName != ''">
                                    <xsl:value-of select="/vcf/@variantTsvFileName"/>
                                    <span style="font-weight: normal;">
                                        <br/>variant TSV file line count (including header): <xsl:value-of select="/vcf/@variantTsvFileLineCount"/>
                                        <br/>number of annotated variants displayed below: <xsl:value-of select="/vcf/@filteredAnnotatedVariantCount"/>
                                    </span>
                                </xsl:when>
                                <xsl:otherwise>
                                    <span style="color: red">*** NO VARIANT TSV FILE IDENTIFIED ***</span>
                                    <span style="font-weight: normal;">
                                        <br/>This report was likely created in error.
                                    </span>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>exon BED file</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:value-of select="/vcf/@exonBedFileName"/>
                        </td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>amplicon BED file</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:value-of select="/vcf/@ampliconBedFileName"/>
                        </td>
                    </tr>
                    <tr style="vertical-align: top;">
                        <td>Do NOT Call file</td>
                        <td>:</td>
                        <td style="font-weight: bold;">
                            <xsl:value-of select="/vcf/@doNotCallFileName"/>
                        </td>
                    </tr>
                </table>

                <ul>
                    <li>QC rules are applied to bases <i>in the coding region</i> of each locus:
                        <ul>
                            <li>pass: <i>all</i> bases read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[4]/@name" disable-output-escaping="yes"/> times</li>
                            <li>warn: <i>all</i> bases read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[3]/@name" disable-output-escaping="yes"/> or <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[4]/@name" disable-output-escaping="yes"/> times</li>
                            <li>fail: <i>any</i> base read <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[1]/@name" disable-output-escaping="yes"/> or <xsl:value-of select="/vcf/geneExons/geneExon[1]/bins/bin[2]/@name" disable-output-escaping="yes"/> times</li>
                        </ul>
                    </li>
                    <li>Coding regions and amplicons are specified by vendor.</li>
                    <li>If the gVCF file contains multiple entries for the same position (e.g., indels), the maximum read depth value is reported here.</li>
                    <li>After selecting variants for export, <a id="exportLink" href="#">click here</a> to see them as a text document suitable for cut-and-paste operations.</li>
                </ul>
            
                <table id="qcReportTable" class="dataTable">
                    <thead>
                        <tr>
                            <th></th>
                            <th colspan="5">gene/exon</th>
                            <th colspan="{count(/vcf/geneExons/geneExon[1]/bins/bin)}">base count by read depth</th>
                        </tr>
                        <tr>
                            <th style="text-align: center;">
                                <a href="#" id="geneExonExpandCollapseAllButton" style="color: blue; text-decoration: none; font-size: large; font-weight: bold;">+</a>
                            </th>
                            <th>QC</th>
                            <th>name</th>
                            <th>% exon<br/>reported</th>
                            <th>locus</th>
                            <th>variant</th>
                            <xsl:for-each select="/vcf/geneExons/geneExon[1]/bins/bin">
                                <th>
                                    <xsl:value-of select="@name" disable-output-escaping="yes"/> 
                                    <br/>reads</th>
                            </xsl:for-each>
                        </tr>
                    </thead>
                    <xsl:for-each select="/vcf/geneExons/geneExon">
                        <xsl:variable name="weight">
                            <xsl:choose>
                                <xsl:when test="@variantCalled = 'true'">bold</xsl:when>
                                <xsl:otherwise>normal</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="color">
                            <xsl:choose>
                                <xsl:when test="@qc = 'fail'">red</xsl:when>
                                <xsl:when test="@qc = 'warn'">yellow</xsl:when>
                                <xsl:when test="@qc = 'pass'">green</xsl:when>
                                <xsl:otherwise></xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:comment>Tom Addition of test doNotCall</xsl:comment>
                        <tr style="font-weight: {$weight};" class="geneExon_parent">
                            <td style="text-align: center; width: 20px;">
                                <a href="#" id="geneExon{position()}" class="geneExonExpandCollapseButton" style="color: blue; text-decoration: none; font-size: large; font-weight: bold;">+</a>
                            </td>
                            <td style="background-color: {$color};" data-export-label="qc">
                                <xsl:value-of select="@qc"/>
                            </td>
                            <td data-export-label="exon">
                                <xsl:value-of select="@name"/>
                                <br/>
                                <span style="font-size: x-small">
                                    Ensembl ID: <a href="http://www.ensembl.org/id/{@ensemblTranscriptId}">
                                        <xsl:value-of select="@ensemblExonId"/>
                                    </a>
                                    <br/>
                                    RefSeq accession no.: <a href="http://www.ncbi.nlm.nih.gov/nuccore/{@refSeqAccNo}">
                                        <xsl:value-of select="@refSeqAccNo"/>
                                    </a>
                                    <br/>
                                    vendor ID: <xsl:value-of select="@vendorGeneExonName"/>
                                </span>
                            </td>
                            <td style="text-align: right;">
                                <xsl:value-of select="format-number(@pctOfExon, '##0')"/>
                            </td>
                            <td data-export-label="locus">
                                <a href="http://127.0.0.1:60151/load?file={../../@bedBamVcfFileUrlsAsString}&amp;locus={@chr}:{@startPos}-{@endPos}&amp;genome=hg19&amp;merge=false">
                                    <xsl:value-of select="@chr"/>:<xsl:value-of select="@startPos"/>-<xsl:value-of select="@endPos"/>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="@variantCalled"/>
                                <xsl:if test="@variantAnnotated = 'true'"> (annotated)</xsl:if>
                                <xsl:if test="@onlyContainsDoNotCallAlways = 'true'">   <br/>Do Not Call, on lab list <br/> of definative do-not-calls</xsl:if>                                   
                            </td>
                            <xsl:for-each select="bins/bin">
                                <td class="readHistogram" style="text-align: right; width: 40px;" data-pct="{@pct}">
                                    <xsl:value-of select="@count"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                        <tr style="display: none;" class="tablesorter-childRow geneExon_child geneExon{position()}_child">
                            <td colspan="{count(bins/bin) + 6}" style="background: white;">
                                <div id="geneExon{position()}_div">
                                    <script type="text/javascript">

                                        function geneExon<xsl:value-of select="position()"/>_drawChart() {

                                        function a(parentElement, element, eldict) { 
                                        el = $(document.createElementNS('http://www.w3.org/2000/svg', element));
                                        el.attr(eldict).appendTo(parentElement);
                                        return el;
                                        }

                                        // don't draw the chart if it has already been drawn
                                        if($("#geneExon<xsl:value-of select="position()"/>_div svg").length > 0) {
                                        return;
                                        }

                                        // draw chart using Google Charts
                                        var data = {
                                        cols:[
                                        {id:'pos', label:'pos', type:'number'}
                                        ,{type:'string', role:'annotation'}
                                        ,{type:'string', role:'annotationText'}
                                        ,{id:'readDepth', label:'reads', type:'number'}
                                        ,{id:'qcThreshold', label:'QC level', type:'number'}
                                        ]
                                        ,rows:[
                                        <xsl:for-each select="bases/base">
                                            <xsl:if test="position() != 1">,</xsl:if>{c:[{v:<xsl:value-of select="@pos"/>}, {v:'<xsl:value-of select="@variant"/>'}, {v:'<xsl:value-of select="@variantText"/>'}, {v:<xsl:value-of select="@totalReadDepth"/>}, {v:<xsl:value-of select="../../bins/bin[last()]/@startCount"/>}]}
                                        </xsl:for-each>
                                        ]};
                                        var dataTable = new google.visualization.DataTable(data);
                                        var dataView = new google.visualization.DataView(dataTable);
                                        var chart = new google.visualization.LineChart(document.getElementById('geneExon<xsl:value-of select="position()"/>_div'));
                                        chart.draw(dataView, { colors: ['blue', 'red'], annotations: { style: 'line' } });

                                        // add the amplicon guides to chart (custom SVG)
                                        var amplicons = [
                                        <xsl:for-each select="amplicons/amplicon">
                                            <xsl:if test="position() != 1">,</xsl:if>{name:'<xsl:value-of select="@name"/>', startPos:<xsl:value-of select="@startPos"/>, endPos:'<xsl:value-of select="@endPos"/>'}
                                        </xsl:for-each>
                                        ];
                                        var svg = $('#geneExon<xsl:value-of select="position()"/>_div svg');
                                        var svgHeight = parseInt(svg.css('height')) + (30 * amplicons.length);
                                        var chartBoundingBox = chart.getChartLayoutInterface().getChartAreaBoundingBox();
                                        var xScale =  (1.0 * chartBoundingBox.width) / (1.0 * (<xsl:value-of select="bases/base[last()]/@pos"/> - <xsl:value-of select="bases/base[1]/@pos"/>));
                                        $('#geneExon<xsl:value-of select="position()"/>_div >:first-child').css({ height:svgHeight + 'px' });
                                        $('#geneExon<xsl:value-of select="position()"/>_div svg').css({ height:svgHeight + 'px' });
                                        var g = a(svg, 'g', { class:'amplicons', transform:'translate(' + chartBoundingBox.left + ' ' + (svgHeight - (30 * amplicons.length) - 10) + ') scale(' + xScale + ' 1)' });
                                        for(var x = 0; x &lt; amplicons.length; x++) {
                                        var y = 30 * x;
                                        var color
                                        if(amplicons[x].name.match(/^.*_coding$/) != null) {
                                        color = 'green';
                                        }else if(amplicons[x].name.match(/^.*_Lung$/) != null){color = 'red'}
                                        else {
                                        color = 'gray';
                                        }
                                        if((amplicons[x].startPos >= <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos &lt;= <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                        a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:y, width:(amplicons[x].endPos - amplicons[x].startPos), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'text', { x:(((amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>) * xScale) + 5), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        else if((amplicons[x].startPos &lt; <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos &lt;= <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                        a(g, 'rect', { x:'0', y:y, width:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/>), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'rect', { x:(amplicons[x].endPos - <xsl:value-of select="bases/base[1]/@pos"/> - 1), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'polygon', { points:'0,' + y + ' 0,' + (y + 25) + ' -25,' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'text', { x:'-75', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (<xsl:value-of select="bases/base[1]/@pos"/> - amplicons[x].startPos) + " bp";
                                        a(g, 'text', { x:'5', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        else if((amplicons[x].startPos >= <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos > <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                        a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:y, width:(<xsl:value-of select="bases/base[last()]/@pos"/> - amplicons[x].startPos), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'rect', { x:(amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>), y:'-' + (svgHeight - 90), width:'1', height:(svgHeight - 90 + y), opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'polygon', { points:chartBoundingBox.width + ',' + y + ' ' + chartBoundingBox.width + ',' + (y + 25) + ' ' + (chartBoundingBox.width + 25) + ',' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'text', { x:(chartBoundingBox.width + 27), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (amplicons[x].endPos - <xsl:value-of select="bases/base[last()]/@pos"/>) + " bp";
                                        a(g, 'text', { x:(((amplicons[x].startPos - <xsl:value-of select="bases/base[1]/@pos"/>) * xScale) + 5), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        else if((amplicons[x].startPos &lt; <xsl:value-of select="bases/base[1]/@pos"/>) &amp;&amp; (amplicons[x].endPos > <xsl:value-of select="bases/base[last()]/@pos"/>)) {
                                        a(g, 'rect', { x:'0', y:y, width:(<xsl:value-of select="bases/base[last()]/@pos"/> - <xsl:value-of select="bases/base[1]/@pos"/>), height:'25', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'polygon', { points:'0,' + y + ' 0,' + (y + 25) + ' -25,' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'text', { x:'-75', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (<xsl:value-of select="bases/base[1]/@pos"/> - amplicons[x].startPos) + " bp";
                                        a(g, 'polygon', { points:chartBoundingBox.width + ',' + y + ' ' + chartBoundingBox.width + ',' + (y + 25) + ' ' + (chartBoundingBox.width + 25) + ',' + (y + 12), transform:'scale(' + (1 / xScale) + ' 1)', opacity:'0.5', style:'fill: ' + color + ';' });
                                        a(g, 'text', { x:(chartBoundingBox.width + 27), y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = (amplicons[x].endPos - <xsl:value-of select="bases/base[last()]/@pos"/>) + " bp";
                                        a(g, 'text', { x:'5', y:(y + 15), transform:'scale(' + (1 / xScale) + ' 1)', style:'font-size: small;' }).context.textContent = amplicons[x].name;
                                        }
                                        }

                                        }

                                    </script>
                                </div>
                                <xsl:if test="count(variants/variant) > 0">
                                    <div style="font-size: small; padding: 10px;">
                                        <h3>Filtered and Annotated Variant(s)</h3>
                                        <table class="dataTable">
                                            <thead>
                                                <tr>
                                                    <th>export?</th>
                                                    <th>gene</th>
                                                    <th>coordinate</th>
                                                    <th>filters<sup>*</sup></th>
                                                    <th>consequence</th>
                                                    <th>genotype</th>
                                                    <th>AVF</th>
                                                    <th>RefSeq</th>
                                                    <th>cDNA</th>
                                                    <th>amino acid</th>
                                                    <th>dbSNP</th>
                                                    <th>MAF</th>
                                                    <th>COSMIC ID</th>
                                                    <xsl:comment>//TOM ADDITION
                                                        //adding this because sometimes the tsv file from variant studio is wrong and it is really in cosmic, creating a work around
                                                    </xsl:comment>
                                                    <th>COSMIC (alternate)</th>
                                                    <th>call status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <xsl:for-each select="variants/variant">
                                                    <tr class="filteredAnnotatedVariant">
                                                        <td style="text-align: center;">
                                                            <input type="checkbox" class="exportCheckbox"/>
                                                        </td>
                                                        <td data-export-label="gene">
                                                            <xsl:value-of select="@gene"/>
                                                        </td>
                                                        <td data-export-label="coordinate">chr<xsl:value-of select="@chr"/>:<xsl:value-of select="@coordinate"/></td>
                                                        <td data-export-label="filters">
                                                            <xsl:value-of select="@filters"/>
                                                        </td>
                                                        <td data-export-label="consequence">
                                                            <xsl:value-of select="@consequence"/>
                                                        </td>
                                                        <td data-export-label="genotype">
                                                            <xsl:value-of select="@genotype"/>
                                                        </td>
                                                        <td data-export-label="avf" style="text-align: right;">
                                                            <xsl:value-of select="@altVariantFreq"/>
                                                        </td>
                                                        <td data-export-label="variantRefSeqAccNo">
                                                            <xsl:value-of select="@refSeqAccNoHgvsc"/>
                                                        </td>
                                                        <td data-export-label="cDna">
                                                            <xsl:value-of select="@hgvsc"/>
                                                        </td>
                                                        <td data-export-label="aminoAcid">
                                                            <xsl:value-of select="@hgvsp"/>
                                                        </td>
                                                        <td>
                                                            <a href="http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?{@dbSnpIdPrefix}={@dbSnpIdSuffix}">
                                                                <xsl:value-of select="@dbSnpIdPrefix"/>
                                                                <xsl:value-of select="@dbSnpIdSuffix"/>
                                                            </a>
                                                        </td>
                                                        <td data-export-label="maf" style="text-align: right;">
                                                            <xsl:value-of select="@alleleFreqGlobalMinor"/>
                                                        </td>
                                                        <td>
                                                            <a href="http://cancer.sanger.ac.uk/cosmic/search?q={@cosmicId}">
                                                                <xsl:value-of select="@cosmicId"/>
                                                            </a>
                                                        </td>
                                                        <xsl:comment> //TOM ADDITION
                                                            //I created a new string stating the gene mutation to be searched in Cosmic ie KIT c.2586G>C 
                                                        </xsl:comment>
                                                        <td>
                                                            <a href="http://cancer.sanger.ac.uk/cosmic/search?q={@geneMutation}">
                                                                <xsl:value-of select="@geneMutation"/>
                                                            </a>
                                                        </td>
                                                        <td width="5%"><xsl:value-of select="@typeOfDoNotCall"/></td>
                                                    </tr>
                                                </xsl:for-each>
                                            </tbody>
                                        </table>
                                        <br/>
                                        <table class="footNoteTable">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 1px solid black;">
                                                    <sup>*</sup>filter descriptions</td>
                                            </tr>
                                            <tr>
                                                <td>LowDP</td>
                                                <td>= low coverage (DP tag), therefore no genotype called</td>
                                            </tr>
                                            <tr>
                                                <td>SB</td>
                                                <td>= variant strand bias too high</td>
                                            </tr>
                                            <tr>
                                                <td>PB</td>
                                                <td>= probe pool bias - variant not found, or found with low frequency, in one of two probe pools</td>
                                            </tr>
                                        </table>
                                    </div>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>

                <p>Copyright &#169; 2014 Geoffrey H. Smith (geoffrey.hughes.smith@gmail.com)</p>
            
                <div id="exportDialog" style="display: none; font-family: Arial; font-size: 9pt;" title="export selected variant(s)">
                    <script type="text/javascript">

                        function showExportDialog() {

                        var interpText = "";
                        var resultsText = "";
                        var failedExonsText = "";
                    
                        {
                        var exportMap = new Object();
                        $(".exportCheckbox:checked").each(function() {
                        $(this).parents("tr.geneExon_child").prev("tr").find("td[data-export-label]").each(function() {
                        if($(this).attr("data-export-label") == "exon") {
                        var re = /(.*)[\s\n]*Ensembl ID: (.*)[\s\n]*RefSeq accession no\.: (.*)[\s\n]*vendor ID: (.*)/gm;
                        var match = re.exec($(this).text());
                        exportMap["exonName"] = match[1];
                        exportMap["exonEnsemblId"] = match[2];
                        exportMap["refSeqAccNo"] = match[3];
                        exportMap["exonVendorId"] = match[4];
                        //Tom Addition
                        var re2 = /[a-z][a-z](\d*)/;
                        var match2 = re2.exec(exportMap.exonName);
                        exportMap["exonName_numOnly"]=match2[1];
                        }
                        else {
                        exportMap[$(this).attr("data-export-label")] = $(this).text();
                        }
                        });
                        $(this).parents("tr.filteredAnnotatedVariant").find("td[data-export-label]").each(function() {
                        exportMap[$(this).attr("data-export-label")] = $(this).text();
                        });
                        //Tom addition
                         var re3 = /chr(.*):(.*)/;
                        var match3 = re3.exec(exportMap.coordinate);
                        exportMap["chr_num"]=match3[1];
                        exportMap["coordinate_num"]=match3[2];
                        var re4 = /chr\d{1,}:(.*)/;
                        var match4 = re4.exec(exportMap.locus);
                        exportMap["locus_numOnly"]=match4[1];
                        
                        //Tom edits here
                        interpText += "&lt;b>POSITIVE for " + exportMap.gene + " " + exportMap.cDna + " ("+ exportMap.aminoAcid + ")"+" in approximately ";
                        interpText += + Math.round(exportMap.avf) + "% of alleles." + "&lt;/b>&lt;br/>&lt;br/>";
                        resultsText += "&lt;b>" + exportMap.gene + " " + exportMap.cDna + " (" + exportMap.aminoAcid + ")" + " details:" + "&lt;/b>&lt;br/>";
                        resultsText += "Allele frequency: " + exportMap.avf + "%" + "&lt;br/>";
                        resultsText += "Chromosome " + exportMap.chr_num + " coordinate " + exportMap.coordinate_num + "&lt;br/>";
                        //resultsText += "In gene region: " + exportMap.gene + " exon " + exportMap.exonName_numOnly + "( Ensembl ID: " + exportMap.exonEnsemblId + "; RefSeq accession no: " + exportMap.refSeqAccNo + "; chromosome " + exportMap.chr_num + ":" + exportMap.locus_numOnly + ")&lt;br/>&lt;br/>";
                        resultsText += "RefSeq accession no: " + exportMap.variantRefSeqAccNo + "&lt;br/>&lt;br/>";
 
                        });
                        }

                        {
                        var exportMap = new Object();
                        $("tr.geneExon_parent").each(function() {
                        if($(this).find("td[data-export-label='qc']").text() == "fail") {
                        $(this).find("td[data-export-label]").each(function() {
                        if($(this).attr("data-export-label") == "exon") {
                        var re = /(.*)[\s\n]*Ensembl ID: (.*)[\s\n]*RefSeq accession no\.: (.*)[\s\n]*vendor ID: (.*)/gm;
                        var match = re.exec($(this).text());
                        exportMap["exonName"] = match[1];
                        exportMap["exonEnsemblId"] = match[2];
                        exportMap["refSeqAccNo"] = match[3];
                        exportMap["exonVendorId"] = match[4];
                        }
                        else {
                        exportMap[$(this).attr("data-export-label")] = $(this).text();
                        }
                        });
                        exportMap["data-pct-failed"] = parseInt($(this).find("td[data-pct]").eq(0).attr("data-pct")) + parseInt($(this).find("td[data-pct]").eq(1).attr("data-pct"));
                        failedExonsText += "gene/exon: " + exportMap.exonName + "; " + exportMap.locus + "; pct-of-locus-failing-QC: " + exportMap['data-pct-failed'] + "&lt;br/>";
                        }
                        });
                        }

                        $("#exportDialog").html("Interpretation&lt;br/>&lt;br/>");
                        $("#exportDialog").append(interpText.length > 0 ? interpText + '&lt;b>See comment.&lt;/b>&lt;br/>&lt;br/>' : "No clinically significant variants detected by next-generation sequencing.&lt;br/>&lt;br/>");
                        $("#exportDialog").append("&lt;br/>&lt;br/>&lt;br/>");
                        $("#exportDialog").append("Results&lt;br/>&lt;br/>");
                        $("#exportDialog").append(resultsText.length > 0 ? resultsText : "No clinically significant variants detected by next-generation sequencing.&lt;br/>&lt;br/>");
                        $("#exportDialog").append("The reference assembly is hg19, GRCh37.&lt;br/>&lt;br/>");
                        $("#exportDialog").append("&lt;br/>&lt;br/>&lt;br/>");
                       // $("#exportDialog").append(failedExonsText.length > 0 ? "Portions of the following captured regions were not sequenced sufficiently for clinical interpretation (at least one base in the sequenced portion of the coding region was read less than 500 times):&lt;br/>&lt;br/>" + failedExonsText : "");
                        //Tom Addition
                         $("#exportDialog").append(failedExonsText.length > 0 ? "Portions of the following captured regions were not sequenced sufficiently for clinical interpretation (at least one base in the sequenced portion of the coding region was read less than 500 times):&lt;br/>&lt;br/>" + failedExonsText : "All portions of the captured regions were sequenced sufficiently for clinical interpretation (all bases in the sequenced portion of the coding region were read more than 500 times).");
                        $("#exportDialog").append("&lt;br/>&lt;br/>&lt;br/>");
                        $("#exportDialog").append("See Notes and Test Performed sections below for more information.");

                        $("#exportDialog").dialog({
                        width:$(window).width() * 0.6,
                        height:$(window).height() * 0.8
                        });
                        
                        }
                    
                    </script>
                </div>

            </body>
        
        </html>

    </xsl:template>

</xsl:stylesheet>