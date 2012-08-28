function Queue() {
	this.aElement = new Array();

	Queue.prototype.enQueue = function(vElement) {
		if (arguments.length == 0) {
			return -1;
		}

		for ( var i = 0; i < arguments.length; i++) {
			this.aElement.push(arguments[i]);
		}
		
		return this.aElement.length;
	};

	Queue.prototype.deQueue = function() {
		if (this.aElement.length == 0)
			return null;
		else
			return this.aElement.shift();
	};

	Queue.prototype.getSize = function() {
		return this.aElement.length;
	};

	Queue.prototype.getHead = function() {
		if (this.aElement.length == 0)
			return null;
		else
			return this.aElement[0];
	};

	Queue.prototype.getEnd = function() {
		if (this.aElement.length == 0)
			return null;
		else
			return this.aElement[this.aElement.length - 1];
	};

	Queue.prototype.makeEmpty = function() {
		this.aElement.length = 0;
	};

	Queue.prototype.isEmpty = function() {
		if (this.aElement.length == 0)
			return true;
		else
			return false;
	};

	Queue.prototype.toString = function() {
		var sResult = this.aElement.toString();
		return "[" + sResult + "]";
	};
}