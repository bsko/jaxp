package ru.qweert.martha.domsax;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ApplicationSaxParser extends DefaultHandler {

    private Document html;
    private Node body;
    private String currentElement = "";

    private boolean goods = false;
    private boolean documents = false;
    private boolean contacts = false;

    private Map<String, List<String>> goodsData = new HashMap<>();
    private Map<String, List<String>> documentsData = new HashMap<>();
    private Map<String, List<String>> contactsData = new HashMap<>();

    public ApplicationSaxParser(Document html) {
        this.html = html;
        body = html.getElementsByTagName("div").item(0);
    }

    public Document getHtml() {
        return html;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if(qName.equalsIgnoreCase("GOODS")) {
            goods = true;
        } else if(qName.equalsIgnoreCase("DOCUMENTS")) {
            documents = true;
        } else if(qName.equalsIgnoreCase("CONTACTS")) {
            contacts = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("GOODS")) {
            goods = false;
            createGoodsTable();
        } else if(qName.equalsIgnoreCase("DOCUMENTS")) {
            documents = false;
            createDocumentsTable();
        } else if(qName.equalsIgnoreCase("CONTACTS")) {
            contacts = false;
            createContactsTable();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length).trim();
        if(text.length() > 0) {
            if(goods) {
                addToMap(goodsData, currentElement, text);
            } else if(documents) {
                addToMap(documentsData, currentElement, text);
            } else if(contacts) {
                addToMap(contactsData, currentElement, text);
            } else {
                Element div = html.createElement("div");
                Attr rowClass = html.createAttribute("class");
                rowClass.setValue("row");
                div.setAttributeNode(rowClass);
                div.appendChild(html.createTextNode(currentElement + " : " + text));
                body.appendChild(div);
            }
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    private void createContactsTable() { createTable(contactsData); }

    private void createDocumentsTable() { createTable(documentsData); }

    private void createGoodsTable() { createTable(goodsData); }

    private void createTable(Map<String, List<String>> data) {
        // получаем количество колонок в таблице
        int rowsCount = data.get(data.keySet().iterator().next()).size();

        Element table = html.createElement("table");
        Attr tableClass = html.createAttribute("class");
        tableClass.setValue("table");
        table.setAttributeNode(tableClass);
        Element thead = html.createElement("thead");
        Element tr = html.createElement("tr");
        Element tbody = html.createElement("tbody");
        List<Element> tableRows = new ArrayList<>(rowsCount);
        IntStream.range(0, rowsCount).forEach(index -> {
            Element tabletr = html.createElement("tr");
            tableRows.add(tabletr);
            tbody.appendChild(tabletr);
        });
        thead.appendChild(tr);
        table.appendChild(thead);
        table.appendChild(tbody);
        for (String key : data.keySet()) {
            Element th = html.createElement("th");
            th.appendChild(html.createTextNode(key));
            tr.appendChild(th);
            List<String> datas = data.get(key);
            IntStream.range(0, datas.size()).forEach(idx -> {
                Element td = html.createElement("td");
                td.appendChild(html.createTextNode(datas.get(idx)));
                tableRows.get(idx).appendChild(td);
            });
        }

        body.appendChild(table);
    }

    private void addToMap(Map<String, List<String>> data, String currentElement, String text) {
        if(!data.containsKey(currentElement)) {
            data.put(currentElement, new ArrayList<>());
        }
        data.get(currentElement).add(text);
    }
}
