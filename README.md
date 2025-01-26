# DCOP-Sim

DCOP-Sim is a simulator designed to test and evaluate various Distributed Constraint Optimization Problem (DCOP) algorithms. Built on top of [Sinalgo](https://disco.ethz.ch/sinalgo/), it provides a robust framework for simulating distributed systems and experimenting with different algorithms.

## Supported Algorithms

DCOP-Sim currently supports the following algorithms:
- **DSA**
- **MaxSum**
- **PDSA** 
- **PMaxSum**

## Features

- Modular design for integrating new DCOP algorithms.
- Visualization and logging support via Sinalgo.
- Flexible configuration options for custom scenarios.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/dcop-sim/dcop-sim.git

### Note:
DCOP SIM was tested using java 1.8

## Code sturcture

proj1/
  src/
    crypto/
      dcop/
        dsa/
          vanilla - implementation of dsa
          secure - implementation of pdsa
        maxsum/
          vanilla - implementation of maxsum
          secure - implementation of pmaxsum
        utils/ - contains misc crypto utils such as shamir secret shared and pailler
    projects/
      dcopproj - sinalgo project that manage testing of dcop algorithms

  
