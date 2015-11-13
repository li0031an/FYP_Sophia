<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Fruit Shop Receipt</title>

<?php

		$name = $_POST['userName'];

		$payment = $_POST['paymentButton'];
		$apple = $_POST['appleQuantity'];
		$orange = $_POST['orangeQuantity'];
		$banana = $_POST['bananaQuantity'];

		$appleCost = $apple * 69/100;
		$orangeCost = $orange * 59/100;
		$bananaCost = $banana * 39/100;

		$totalCost = $appleCost + $orangeCost + $bananaCost;

        echo "<h2><center>The Fruit Shop</center></h2>";
        echo "<h3>Dear $name</h3>";
        echo "<h3>Thanks for purchasing with us!</h3>";
        echo "<h3>Here is the receipt for you</h3>";
		
        echo "<hr>";
?>

<center>
    <table>
        <caption> Order Information </caption>
        <tr align = "center">
            <th>Item </th>
            <th>Quantity</th>
            <th>Cost</th>
        </tr>
        <tr align = "center">
            <td>Apple</td>
            <td><?php echo $apple ?></td>
            <td><?php printf ("$ %4.2f", $appleCost) ?></td>
        </tr>
        <tr align = "center">
            <td>Orange</td>
            <td><?php echo$orange?></td>
            <td><?php printf ("$ %4.2f", $orangeCost) ?></td>
        </tr>
        <tr align = "center">
            <td>Banana</td>
            <td><?php echo $banana?></td>
            <td><?php printf ("$ %4.2f", $bananaCost) ?></td>
        </tr>
    </table>

    <hr>

    <table>
        <tr align = "left">
            <td><?php printf ("Your total bill is: $ %5.2f", $totalCost) ?></td>
        </tr align = "left">
        <tr>
            <td><?php printf ("Your chosen method of payment is: %s", $payment)?></td>
        </tr>

    </table>

</center>

<?php
		/* Store the records in the txt file */
		$store = "";
		$file = "order.txt";
		if (!file_exists($file)) {
			/* create the file at the first access */
			$openFile = fopen($file, "w")
				or die("fail creating order.txt");
			$store = "Total number of apples: " . $apple . "\r\nTotal number of oranges: " . $orange . "\r\nTotal number of bananas: " . $banana."\r\n";
		}
		else {
			/* read in and update the records in later accesses */
			$openFile = fopen($file, "a+")
				or die("fail reading from order.txt");
			/* lock the file */
		if (!flock($openFile, LOCK_EX | LOCK_NB)) {
			echo 'unable to obtain lock';
			echo 'new apple amount order: '. $apple; 
			echo 'new orange amount order: '. $orange; 
			echo 'new banana amount order: '. $banana; 
			exit (-1);
		}
			$counter = 0;
			while (!feof($openFile)) {
				// read one line
				$line = fgets($openFile);
				// get the digits from string and discard any other character
				$record = (int) preg_replace("/[^0-9]/", "", $line);
				// use lineCount to judge which line it is, and find the corresponding output line.
				// add the new order and old data together, save to output
				switch ($counter) {
					case 0:
					$record += $apple;
					$store = $store . "Total number of apples: " . $record;
					break;
					case 1:
					$record += $orange;
					$store = $store . "\r\nTotal number of oranges: " . $record;
					break;
					case 2:
					$record += $banana;
					$store = $store . "\r\nTotal number of bananas: " . $record;
					break;
				}
				$counter++;
			}
		}
		ftruncate($openFile, 0);
		// now file is truncated and output is ready, write output to file
		fwrite($openFile, $store);
		fclose($openFile);


		/*

			";
		*/


		//echo "</center>";

?>
</html>