<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Fruit Store Receipt</title>

<?php

		$name = $_POST['userName'];

		$payment = $_POST['paymentButton'];
		$apple = $_POST['appleQuantity'];
		$orange = $_POST['orangeQuantity'];
		$banana = $_POST['bananaQuantity'];

		$appleCost = $apple * 69;
		$orangeCost = $orange * 59;
		$bananaCost = $banana * 39;

		$totalCost = ($appleCost + $orangeCost + $bananaCost)/100;

		/*echo "Quantity of apples: $apple";
		echo "<br/>";
        echo "Quantity of oranges: $orange";
        echo "<br/>";
        echo "Quantity of oranges: $banana";
        echo "<br/>";
        echo "Total Cost: $totalCost";

        echo "<br/>";
        echo "Payment method: $payment";
        */

        echo "<h2><center>The Fruit Shop</center></h2>";
        echo "<h2><center>Receipt for customer: $name</center></h2>";
        echo "<hr>";

echo "<br/>";
?>

<center>
    <table>

        <tr>
            <th>Item </th>
            <th>Quantity</th>
            <th>Cost</th>
        </tr>
        <tr>
            <td>&nbsp; Apple</td>
            <td>&nbsp; <?php echo $apple ?></td>
            <td>&nbsp;<?php echo$apple?></td>
        </tr>
        <tr>
            <td>&nbsp;Orange</td>
            <td>&nbsp;<?php echo$orange?></td>
            <td>&nbsp;<?php echo$orange?></td>
        </tr>
        <tr>
            <td>&nbsp;Banana</td>
            <td>&nbsp;<?php echo $banana?></td>
            <td>&nbsp;<?php echo $banana?></td>
        </tr>
    </table>

    <hr>

    <table>
        <tr>
            <th>Total cost:</th>
            <td><?php echo $totalCost?></td>
        </tr>
        <tr>
            <th>Payment method:</th>
            <td><?php echo $payment?></td>
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