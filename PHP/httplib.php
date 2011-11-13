<?php
/*
 * File: httplib.php
 * Date: 20111029
 * Author: James D. Low
 * URL: http://jameslow.com
 * About: This file contains code for making a http request when cURL isn't avaliable or file_get_contents
 *        isn't enabled to allow getting contents over from a remote location over HTTP. This can be
 *        checked by checking the ini_get("allow_url_fopen") which is set in the php.ini file.
 *        Its modified from the code found here: http://php.net/manual/en/function.fopen.php#58099
 *        There are more examples here: http://php.net/manual/en/function.fsockopen.php
 */

//Class that implements the request
class HTTPRequest {
	const GET = 'GET';
	const PUT = 'PUT';
	const POST = 'POST';
	const URL_ENCODED = 'application/x-www-form-urlencoded';
	const HTTP = 'http';
	const HEADER = 'header';
	const BODY = 'body';
	
	var $fp;			// the socket
	var $url;			// full URL
	var $host;			// HTTP host
	var $protocol;		// protocol (HTTP/HTTPS)
	var $uri;			// request URI
	var $port;			// port
	var $timeout;		// timeout
	var $keepalive;		// keep tcp alive
	
	function __construct($url, $keepalive = false, $timeout = 1000) {
		$this->SetUrl($url);
		$this->timeout = $timeout;
		$this->keepalive = $keepalive;
	}
	
	function SetUrl($url) {
		$this->Close();
		$this->url = $url;
		$this->scanUrl();
	}
	
	function Close() {
		if ($this->fp && is_resource($this->fp)) {
			fclose($this->fp);
			$this->fp = null;
		}
	}
	
	//Scan URL and prepopulate variables
	function scanUrl() {
		$req = $this->url;
		
		$pos = strpos($req, '://');
		$this->protocol = strtolower(substr($req, 0, $pos));
		
		$req = substr($req, $pos+3);
		$pos = strpos($req, '/');
		if($pos === false) {
			$pos = strlen($req);
		}
		$host = substr($req, 0, $pos);
		
		if(strpos($host, ':') !== false) {
			list($this->host, $this->port) = explode(':', $host);
		} else {
			$this->host = $host;
			$this->port = ($this->protocol == 'https') ? 443 : 80;
		}
		
		$this->uri = substr($req, $pos);
		if($this->uri == '') {
			$this->uri = '/';
		}
	}
	
	function SetUri($uri) {
		$this->uri = $uri;
	}
	
	//Encode a value in the header
	function HeaderEncode($header, $value) {
		$crlf = "\r\n";
		return $header.': '.$value.$crlf;
	}
	
	//Basic header common to both GET and POST
	function BasicHeader($method, $params, $headerarray) {
		$crlf = "\r\n";
		if (!$headerarray['Host']) {
			$headerarray['Host'] = $this->host;
		}
		$headerarray['Connection'] = ($this->keepalive?'keep-alive':'close');
		$result = $method.' '.$this->uri.($params==''?'':'?'.$params).' HTTP/1.1'.$crlf;
		foreach ($headerarray as $header => $value) {
			$result .= $this->HeaderEncode($header,$value);
		}
		$result .= $crlf;
		return $result;
	}
	
	//Encode request param
	function ParamEncode($key, $value) {
		return urlencode($key).'='.urlencode($value);
	}
	
	//Encode request params
	function RequestEncode($paramarray) {
		$content = '';
		//Encode all post parameters
		if ($paramarray) {
			foreach($paramarray as $key => $value) {
				if ($content == '') {
					$content = $this->ParamEncode($key,$value);
				} else {
					$content .= '&'.$this->ParamEncode($key,$value);
				}
			}
		}
		return $content;
	}
	
	//Content Lengt
	function ContentLength($content) {
		return strlen($content);
	}
	
	//Get request
	function Get($paramarray = array(), $details = false, $headerarray = array()) {
		$req = $this->BasicHeader(HTTPRequest::GET, $this->RequestEncode($paramarray), $headerarray);
		return $this->Request($req,$details);
	}
	
	//Post request
	function Post($paramarray = array(), $details = false, $headerarray = array()) {
		if (is_array($paramarray)) {
			$content = $this->RequestEncode($paramarray);
		} else {
			$content = $paramarray;
		}
		if (!$headerarray['Content-Type']) {
			$headerarray['Content-Type'] = HTTPRequest::URL_ENCODED;
		}
		if (!$headerarray['Content-Length']) {
			$headerarray['Content-Length'] = $this->ContentLength($content);
		}
		$req = $this->BasicHeader(HTTPRequest::POST,'',$headerarray).$content;
		return $this->Request($req,$details);
	}
	
	//Put request
	function Put($paramarray = array(), $details = false, $headerarray = array()) {
		if (is_array($paramarray)) {
			$content = $this->RequestEncode($paramarray);
		} else {
			$content = $paramarray;
		}
		if (!$headerarray['Content-Length']) {
			$headerarray['Content-Length'] = $this->ContentLength($content);
		}
		$req = $this->BasicHeader(HTTPRequest::PUT,'',$headerarray).$content;
		return $this->Request($req,$details);
	}
	
	//General request
	function Request($req, $details = false) {
		$crlf = "\r\n";
		
		//Fetch the data
		if (!$this->keepalive || !$this->fp || !is_resource($this->fp)) {
			$this->fp = fsockopen(($this->protocol == 'https'?'ssl://':'').$this->host, $this->port);
			stream_set_timeout($this->fp, 0, $this->timeout * 1000);
		}
		fwrite($this->fp, $req);
		$response = '';
		if ($this->keepalive) {
			$line = '';
			$lastbyte = null;
			$contentlength = 0;
			$contentstart = false;
			while($this->fp && is_resource($this->fp) && !feof($this->fp)) {
				if (!$contentstart) {
					$byte = fread($this->fp, 1);
					$response .= $byte;
					$line .= $byte;
					if ($lastbyte == "\r" && $byte == "\n") {
						if ($line == $crlf) {
							$contentstart = true;
						} elseif (strpos(strtolower($line),'content-length') !== false) {
							$contentlength = trim(substr($line, strpos($line,':')+1));
						}
						$line = '';
					}
					$lastbyte = $byte;
				} else {
					if ($contentlength > 0) {
						$response .= fread($this->fp, $contentlength);
					}
					break;
				}
			}
		} else {
			//Read till termination of connection
			while($this->fp && is_resource($this->fp) && !feof($this->fp)) {
				$response .= fread($this->fp, 1024);
			}
			$this->Close();
		}
		
		//Split header and body
		$pos = strpos($response, $crlf . $crlf);
		if($pos === false)
			return($response);
		$header = substr($response, 0, $pos);
		$body = substr($response, $pos + 2 * strlen($crlf));
		
		//Parse headers
		$headers = array();
		$lines = explode($crlf, $header);
		$firsttime = true;
		foreach($lines as $line) {
			if ($firsttime) {
				$codes = explode(" ", $line);
				$code['version'] = $codes[0];
				$code['code'] = intval($codes[1]);
				$code['message'] = $codes[2];
				$firsttime = false;
			}
			if(($pos = strpos($line, ':')) !== false) {
				$headers[strtolower(trim(substr($line, 0, $pos)))] = trim(substr($line, $pos+1));
			}
		}
		
		//Redirection?
		if(isset($headers['location'])) {
			//TODO: eventually handle keep alive here too
			$http = new HTTPRequest($headers['location']);
			return $http->Request($req, $details);
		} else {
			if (strtolower($headers['transfer-encoding']) == 'chunked') {
				$body = $this->unchunkHttp11($body);
			}
			if ($details) {
				$result[HTTPRequest::HTTP] = $code;
				$result[HTTPRequest::HEADER] = $headers;
				$result[HTTPRequest::BODY] = $body;
				return $result;
			} else {
				return $body;
			}
		}
	}
	
	function unchunkHttp11($data) {
		$fp = 0;
		$outData = "";
		while ($fp < strlen($data)) {
			$rawnum = substr($data, $fp, strpos(substr($data, $fp), "\r\n") + 2);
			$num = hexdec(trim($rawnum));
			$fp += strlen($rawnum);
			$chunk = substr($data, $fp, $num);
			$outData .= $chunk;
			$fp += strlen($chunk);
		}
		return $outData;
	}
	
	//Download URL to string, included for backwards compatibilty with versions that did have seperate GET/POST
	function DownloadToString() {
		return $this->Get(null,false);
	}
}

//Simple function to do it quickly
function get_url($url) {
	$http = new HTTPRequest($url);
	return $http->Get();
}
?>