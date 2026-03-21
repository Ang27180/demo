package com.poryectojpa.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HannaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HannaApplication.class, args);

    }
// <!-- Created with Jaspersoft Studio version 7.0.3.final using JasperReports Library version 7.0.3-41034ca841d452f3305ba55b9042260aaa1ab5dd  -->
// <jasperReport name="Blank_A4" language="java" pageWidth="595" pageHeight="842" columnWidth="555" leftMargin="20" rightMargin="20" topMargin="20" bottomMargin="20" uuid="f6268c9e-5b4f-4a46-bd66-3bc4d985adbd">
//     <property name="com.jaspersoft.studio.data.defaultdataadapter" />
//     <parameter name="TITULO" class="java.lang.String"/>
//     <parameter name="NOMBREMASCOTA" class="java.lang.String"/>
//     <query language="sql"><![CDATA[]]></query>
//     <field name="label" class="java.lang.String"/>
//     <field name="valor" class="java.lang.Double"/>
//     <background splitType="Stretch"/>
//     <title height="79" splitType="Stretch">
//         <element kind="textField" uuid="08ef5c15-220c-4327-ad80-9660a60f2de4" x="227" y="24" width="100" height="30">
//             <expression><![CDATA[$P{TITULO}]]></expression>
//         </element>
//         <element kind="image" uuid="b7019e05-f34c-4b3c-ab05-aa3888834268" x="510" y="-1" width="50" height="50">
//             <expression><![CDATA["https://cdn.prod.website-files.com/5f5a53e153805db840dae2db/64e79ca5aff2fb7295bfddf9_github-que-es.jpg"]]></expression>
//         </element>
//     </title>
//     <pageHeader height="38" splitType="Stretch"/>
//     <columnHeader height="383" splitType="Stretch">
//         <element kind="chart" chartType="bar" uuid="45ed520f-37eb-4500-8b75-0a02b8026844" key="" x="170" y="90" width="200" height="200" evaluationTime="Report" linkTarget="Self">
//             <dataset kind="category">
//                 <series>
//                     <seriesExpression><![CDATA["VENTAS"]]></seriesExpression>
//                     <categoryExpression><![CDATA[0]]></categoryExpression>
//                     <valueExpression><![CDATA[$F{valor}]]></valueExpression>
//                     <labelExpression><![CDATA[$F{label}]]></labelExpression>
//                 </series>
//             </dataset>
//             <plot categoryAxisLabelColor="#000000" categoryAxisTickLabelColor="#000000" categoryAxisLineColor="#000000" valueAxisLabelColor="#000000" valueAxisTickLabelColor="#000000" valueAxisLineColor="#000000">
//                 <itemLabel/>
//             </plot>
//         </element>
//     </columnHeader>
//     <detail>
//         <band height="125" splitType="Stretch"/>
//     </detail>
//     <columnFooter height="52" splitType="Stretch">
//         <element kind="textField" uuid="d5601bb1-fc6e-4ce0-92c6-aa7f88022c2e" x="227" y="10" width="100" height="30">
//             <expression><![CDATA[$P{NOMBREMASCOTA}]]></expression>
//         </element>
//     </columnFooter>
//     <pageFooter height="54" splitType="Stretch"/>
//     <summary height="42" splitType="Stretch"/>
// </jasperReport>
}
