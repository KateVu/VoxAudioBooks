package com.katevu.voxaudiobooks.models

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseXML {
    private val TAG = "ParseXML"
    private val FIELD_FILE = "file"
    private val FIELD_TRACK = "track"
    private val FIELD_ALBUM = "album"
    private val FIELD_TITLE = "title"
    private val FIELD_LENGTH = "length"
    private val FIELD_SIZE = "size"
    private val FIELD_ARTIST = "artist"

    var book = BookDetails()

    fun parse(xmlData: String): Boolean {
//        Log.d(TAG, "parse called with $xmlData")

        var status = true
        var textValue = ""
        var field = ""
        var attributeName = ""
        var attributeTrack = ""
        var attributeAlbum = ""
        var attributeArtist = ""
        var attributeTitle = ""
        var attributeLength = ""
        var attributeSize = ""

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
                        when (tagName) {
                            FIELD_FILE -> {
                                if (xpp.attributeCount > 0) attributeName = xpp.getAttributeValue(0)
                            }
                        }
                    }

                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
//                        Log.d(TAG, "parse: Ending tag for $tagName")
                        when (tagName) {
                            FIELD_FILE -> {
                                when (field) {
                                    "track" -> {
                                        var track = Track("bookCover", "baseURl", attributeTrack, attributeName, attributeTitle, attributeAlbum, attributeArtist, attributeLength, attributeSize, 0)
                                        book.listTracks.add(track)
//                                        Log.d(TAG, ".parseXML ${book.listTracks}")
                                    }
                                }
                                attributeName = ""
                                attributeTrack = ""
                                attributeAlbum = ""
                                attributeArtist = ""
                                attributeTitle = ""
                                attributeLength = ""
                                attributeSize = ""
                                field = ""
                            }
                            FIELD_TRACK -> {
                                attributeTrack = textValue
                                field = "track"
                            }
                            FIELD_ALBUM -> {
                                attributeAlbum = textValue
                            }

                            FIELD_ARTIST -> {
                                attributeArtist = textValue
                            }
                            FIELD_TITLE -> {
                                attributeTitle = textValue
                            }
                            FIELD_LENGTH -> {
                                attributeLength = textValue
                            }
                            FIELD_SIZE -> {
                                attributeSize = textValue
                            }
                        }
                    }
                }

                //Nothing to do
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }
        return status
    }
}
