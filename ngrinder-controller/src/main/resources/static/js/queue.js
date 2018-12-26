function Queue(size) {
	this.size = Math.ceil(size);
	this.aElement = new Array(this.size);
	for (var i = 0; i < this.size; i++) {
		this.aElement[i] = 0;
	}
}


Queue.prototype.enQueue = function (vElement) {
	if (arguments.length == 0) {
		return;
	}

	for (var i = 0; i < arguments.length; i++) {
		this.deQueue();
		this.aElement.push(arguments[i]);
	}
};

Queue.prototype.deQueue = function () {
	if (this.aElement.length == 0)
		return null;
	else
		return this.aElement.shift();
};

Queue.prototype.getArray = function () {
	return this.aElement;
};

Queue.prototype.isEmpty = function () {
	return this.aElement.length == 0;
};

Queue.prototype.toString = function () {
	var sResult = this.aElement.toString();
	return "[" + sResult + "]";
};