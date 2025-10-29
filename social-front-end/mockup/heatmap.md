# Parking Occupancy – Basel

## Bar chart
```html
<h3>Parking Occupancy Heatmap</h3>
<div id="heat"></div>
<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
<script>
var data = [{
  z: [[45,50,60,70,80,65],[25,30,35,40,50,60],[40,45,55,65,70,80],[100,100,100,100,100,100]],
  x: ['8h','10h','12h','14h','16h','18h'],
  y: ['Messe','Kunstmuseum','Europe','Claramatte'],
  type: 'heatmap',
  colorscale: 'Viridis'
}];
Plotly.newPlot('heat', data);
</script>

