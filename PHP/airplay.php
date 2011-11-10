<?php
//Requires a http library, if you want you can copy and past it and include it all in one file if that suites your purposes
require_once 'httplib.php';

//This class you can use in any normal PHP program
//You'll have to  not exit the script if you want an image to remain
class AirPlay {
	const NONE = 'None';
	const SLIDE_LEFT = 'SlideLeft';
	const SLIDE_RIGHT = 'SlideRight';
	const DISSOLVE = 'Dissolve';
	var $http = null;
	var $hostname = null;
	var $port = 7000;
	
	function __construct($hostname, $port = 7000) {
		$this->hostname = $hostname;
		$this->port = $port;
	}
	function getHttp() {
		if ($this->http == null) {
			$this->http = new HTTPRequest('http://'.$this->hostname.':'.$this->port,1000,true);
		}
		return $this->http;
	}
	function stop() {
		$http = $this->getHttp();
		$http->SetUrl('http://'.$this->hostname.':'.$this->port.'/stop');
		$http->Post('');
		$http->Close();
		$this->http = null;
	}
	function image($image) {
		$http = $this->getHttp();
		$http->SetUrl('http://'.$this->hostname.':'.$this->port.'/photo');
		$http->Put($image);
	}
	function imageFile($file) {
		$this->image(file_get_contents($file));
	}
}

//The rest on is for running from the command line
if (PHP_SAPI === 'cli') {
	//Class from: http://clickontyler.com/blog/2008/11/parse-command-line-arguments-in-php/
	class Args {
		private $flags;
		public $args;

		public function __construct()
		{
			$this->flags = array();
			$this->args  = array();

			$argv = $GLOBALS['argv'];
			array_shift($argv);

			for($i = 0; $i < count($argv); $i++)
			{
				$str = $argv[$i];

				// --foo
				if(strlen($str) > 2 && substr($str, 0, 2) == '--')
				{
					$str = substr($str, 2);
					$parts = explode('=', $str);
					$this->flags[$parts[0]] = true;

					// Does not have an =, so choose the next arg as its value
					if(count($parts) == 1 && isset($argv[$i + 1]) && preg_match('/^--?.+/', $argv[$i + 1]) == 0)
					{
						$this->flags[$parts[0]] = $argv[$i + 1];
					}
					elseif(count($parts) == 2) // Has a =, so pick the second piece
					{
						$this->flags[$parts[0]] = $parts[1];
					}
				}
				elseif(strlen($str) == 2 && $str[0] == '-') // -a
				{
					$this->flags[$str[1]] = true;
					if(isset($argv[$i + 1]) && preg_match('/^--?.+/', $argv[$i + 1]) == 0)
						$this->flags[$str[1]] = $argv[$i + 1];
				}
				elseif(strlen($str) > 1 && $str[0] == '-') // -abcdef
				{
					for($j = 1; $j < strlen($str); $j++)
						$this->flags[$str[$j]] = true;
				}
			}

			for($i = count($argv) - 1; $i >= 0; $i--)
			{
				if(preg_match('/^--?.+/', $argv[$i]) == 0)
					$this->args[] = $argv[$i];
				else
					break;
			}

			$this->args = array_reverse($this->args);
		}

		public function flag($name)
		{
			return isset($this->flags[$name]) ? $this->flags[$name] : false;
		}
	}
	function usage() {
		echo "commands: -s {stop} -i file {image}\n";
		echo "php ".$GLOBALS['argv'][0]." -h {hostname} [-p {port}] command\n";
	}
	function waitforuser() {
		echo 'Press return to quit';
		fgets(STDIN);
	}
	$args = new Args();
	$host = $args->flag('h');
	if ($host == null || $host == '' || $host === false) {
		usage();
		exit(1);
	} else {
		$port = $args->flag('p');
		if ($port) {
			$airplay = new AirPlay($host,$port);
		} else {
			$airplay = new AirPlay($host);
		}
		if ($args->flag('s')) {
			$airplay->stop();
		} elseif (($file = $args->flag('i'))) {
			$airplay->imageFile($file);
			waitforuser();
		} else {
			usage();
		}
	}
}
?>