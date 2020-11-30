'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  selectEntity,
  genNewEntity,
  replyPostEntity,
  reqPostMedia,
  genNewMessage,
  genNewMessageReply,
  selectMsgFromList,
  processUploadReply,
  selectImageToDownload
}


const fs = require('fs')
const Faker = require('faker')
const fetch = require('node-fetch')

var imagesIds = [];
var images = [];
var entityIds = [];
var loaded = false;

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
var statsPrefix = [ ["/media/","GET"]
	]

// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
	var i = 0;
	for( i = 0; i < statsPrefix.length; i++) {
		if( str.startsWith( statsPrefix[i][0]) && method == statsPrefix[i][1])
			return method + ":" + statsPrefix[i][0];
	}
	return method + ":" + str;
}

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Loads data about images from disk
function loadData() {
	if( loaded)
		return;
	var basefile
	if( fs.existsSync('entities.data')) {
		let str = fs.readFileSync('entities.data','utf8')
		entityIds = JSON.parse(str)
	} 
	fs.readdirSync('images').forEach(file => {
		if( file.endsWith('.jpg')) {
			var img  = fs.readFileSync('images/' + file)
			images.push( img)
		}
	});
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function reqPostMedia(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if( typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
    return next()
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if( imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
}


/**
 * Generate data for a new entity. Starts by loading data if it was not loaded yet.
 * Stores in the variables:
 * name : name of the entity
 * description : text with small description
 * businessType : text with business type
 * listed : whether the entity should be listed or not 
 */
function genNewEntity(context, events, done) {
	loadData();
	context.vars.name = `${Faker.company.companyName()}`
	context.vars.description = `${Faker.company.catchPhrase()}`
	return done()
}

/**
 * Select an entity, if one exists.
 * Stores in the variables:
 * entityId : id of entity
 */
function selectEntity(context, events, done) {
	loadData();
	if( entityIds.length > 0) {
		context.vars.entityId = entityIds.sample()
	} else {
		delete context.vars.entityId
	}
	return done()
}

/**
 * Process reply of the post entity. 
 */
function replyPostEntity(requestParams, response, context, ee, next) {
	if( response.statusCode == 200) {
		let entity = response.toJSON().body
		entityIds.push(entity.id)
		fs.writeFileSync('entities.data', JSON.stringify(entityIds))
	}
    return next()
}


/**
 * Generate data for a new message. Starts by loading data if it was not loaded yet.
 * Stores in the variables:
 * entityId : id of entity for the message
 * fromWho : email
 * msg : message
 */
function genNewMessage(context, events, done) {
	loadData();
	if( entityIds.length > 0) {
		context.vars.entityId = entityIds.sample()
		context.vars.fromWho = `${Faker.name.firstName()} ${Faker.name.lastName()}`
		context.vars.msg = `${Faker.lorem.paragraph()}`
		delete context.vars.replyToId
	} else {
		delete context.vars.entityId
	}
	return done()
}

/**
 * Process reply to list of messages, selecting a message.
 * Stores in the variables:
 * msgId : id of message
 */
function selectMsgFromList(requestParams, response, context, ee, next) {
	if( response.statusCode == 200) {
		let msgs = JSON.parse( response.body)
		if( msgs.length > 0) {
			context.vars.msgJSON = msgs[random(msgs.length)]
		} else {
			delete context.vars.msgJSON
		}
	}
    return next()
}

/**
 * Generate data for a new message reply to message stored in variable msg. Starts by loading data if it was not loaded yet.
 * Stores in the variables:
 * entityId : id of entity for the message
 * fromWho : email
 * msg : message
 * replyToId : id of message to reply to
 */
function genNewMessageReply(context, events, done) {
	loadData();
	if( typeof context.vars.msgJSON !== undefined) {
		context.vars.entityId = context.vars.msgJSON.entityId
		context.vars.fromWho = `${Faker.name.firstName()} ${Faker.name.lastName()}`
		context.vars.msg = `${Faker.lorem.paragraph()}`
		context.vars.replyToId = context.vars.msgJSON.id
	} else {
		delete context.vars.entityId
	}
	return done()
}

