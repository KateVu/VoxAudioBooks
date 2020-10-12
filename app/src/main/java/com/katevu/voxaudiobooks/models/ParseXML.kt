package com.katevu.voxaudiobooks.models

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseXML {
    private val TAG = "ParseXML"

    var book = BookDetails()

    fun parse(xmlData: String): Boolean {
//        Log.d(TAG, "parse called with $xmlData")

        var status = true
        var inEntry = false
        var textValue = ""
        var field = ""
        var tagAttribute: String = ""


        try {
            var factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = BookDetails()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xpp.name?.toLowerCase() //should use safe call operator

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        //Log.d(TAG,"parse: Starting tag for $tagName")
                        if ((tagName == "file") && (xpp.attributeCount > 0)){
                            tagAttribute = xpp.getAttributeValue(0)
//                            Log.d(TAG, "tagAttribute: $tagAttribute")
                        }
                    }

                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
//                        Log.d(TAG, "parse: Ending tag for $tagName")
                        when (tagName) {
                            "file" -> {
                                when (field) {
                                    "thumbnail" -> {
                                        book.bookThumbnail = tagAttribute
                                        Log.d(TAG, ".parse bookThumbnail: $tagAttribute")
                                    }
                                    "bookCover" -> book.bookCover = tagAttribute
                                    "track" -> book.listTracks.add(tagAttribute)
                                }

                                tagAttribute = ""
                                field = ""
                            }
                            "format" -> {
                                field = when (textValue) {
                                    "JPEG" -> "bookCover"
                                    "JPEG Thumb" -> "thumbnail"
                                    else -> ""
                                }
                            }
                            "track" -> {
                                field = "track"
                            }
                        }
                    }
                }

                //Nothing to do
                eventType = xpp.next()
            }

//            for (app in applications) {
//                Log.d(TAG,"*****************************")
//                Log.d(TAG, app.toString())
//            }

        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }

        return status
    }
}
